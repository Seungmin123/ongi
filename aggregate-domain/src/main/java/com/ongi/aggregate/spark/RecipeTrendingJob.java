package com.ongi.aggregate.spark;

import java.sql.Connection;
import org.apache.spark.sql.*;
import org.apache.spark.sql.expressions.Window;
import org.apache.spark.sql.expressions.WindowSpec;
import org.apache.spark.sql.streaming.StreamingQuery;
import org.apache.spark.sql.streaming.Trigger;
import org.apache.spark.sql.types.*;

import java.sql.*;
import java.time.Instant;
import java.util.Iterator;

import redis.clients.jedis.*;

import static org.apache.spark.sql.functions.*;

public class RecipeTrendingJob {

	private static final int TOP_N_DEFAULT = 200;
	private static final int REDIS_TTL_SECONDS_DEFAULT = 3600; // 1 hour

	public static void main(String[] args) throws Exception {

		String kafkaBootstrap = env("KAFKA_BOOTSTRAP", "localhost:9092");
		String redisHost = env("REDIS_HOST", "localhost");
		int redisPort = Integer.parseInt(env("REDIS_PORT", "6379"));

		String mysqlUrl = env("MYSQL_URL", "jdbc:mysql://localhost:3306/recipe?useSSL=false&serverTimezone=UTC");
		String mysqlUser = env("MYSQL_USER", "root");
		String mysqlPass = env("MYSQL_PASS", "root");

		String checkpointDir = env("CHECKPOINT_DIR", "/tmp/spark-ckpt/recipe-trending");
		String windowSize = env("WINDOW_SIZE", "10 minutes");
		String slideSize = env("SLIDE_SIZE", "1 minute");
		String watermark = env("WATERMARK", "2 minutes");

		int topN = Integer.parseInt(env("TOP_N", String.valueOf(TOP_N_DEFAULT)));
		int redisTtlSeconds = Integer.parseInt(env("REDIS_TTL_SECONDS", String.valueOf(REDIS_TTL_SECONDS_DEFAULT)));

		SparkSession.Builder b = SparkSession.builder()
			.appName("recipe-trending-java")
			.master(System.getenv().getOrDefault("SPARK_MASTER", "local[*]"))
			.config("spark.sql.shuffle.partitions", System.getenv().getOrDefault("SPARK_SHUFFLE_PARTITIONS", "8"));

		String driverMem = System.getenv("SPARK_DRIVER_MEMORY");
		if (driverMem != null && !driverMem.isBlank()) {
			b = b.config("spark.driver.memory", driverMem);
		}

		SparkSession spark = b.getOrCreate();

		// JSON schema
		StructType schema = new StructType(new StructField[]{
			new StructField("eventId", DataTypes.StringType, true, Metadata.empty()),
			new StructField("eventType", DataTypes.StringType, true, Metadata.empty()),
			new StructField("occurredAt", DataTypes.StringType, true, Metadata.empty()),
			new StructField("recipeId", DataTypes.LongType, true, Metadata.empty()),
			new StructField("userId", DataTypes.LongType, true, Metadata.empty())
		});

		Dataset<Row> raw = spark.readStream()
			.format("kafka")
			.option("kafka.bootstrap.servers", kafkaBootstrap)
			.option("subscribe", "recipe.engagement.v1")
			.option("kafka.request.timeout.ms", "60000")
			.option("kafka.session.timeout.ms", "45000")
			.option("failOnDataLoss", "false")
			.load();

		Column eventTime = coalesce(
			expr("try_to_timestamp(occurredAt, \"yyyy-MM-dd'T'HH:mm:ss.SSSSSSXXX\")"),
			expr("try_to_timestamp(occurredAt, \"yyyy-MM-dd'T'HH:mm:ss.SSSXXX\")"),
			expr("try_to_timestamp(occurredAt, \"yyyy-MM-dd'T'HH:mm:ssXXX\")"),
			expr("try_to_timestamp(occurredAt, \"yyyy-MM-dd'T'HH:mm:ss.SSSSSS\")"),
			expr("try_to_timestamp(occurredAt, \"yyyy-MM-dd'T'HH:mm:ss.SSS\")"),
			expr("try_to_timestamp(occurredAt, \"yyyy-MM-dd'T'HH:mm:ss\")")
		);

		Dataset<Row> events = raw
			.selectExpr("CAST(value AS STRING) AS json")
			.select(from_json(col("json"), schema).as("e"))
			.select("e.*")
			.withColumn("eventTime", eventTime)
			.filter(col("eventTime").isNotNull());

		// score: view=1, like=3, bookmark=5 (취소는 음수)
		Column scoreCol =
			when(col("eventType").equalTo("RECIPE_BOOKMARKED"), lit(5))
				.when(col("eventType").equalTo("RECIPE_UNBOOKMARKED"), lit(-5))
				.when(col("eventType").equalTo("RECIPE_LIKED"), lit(3))
				.when(col("eventType").equalTo("RECIPE_UNLIKED"), lit(-3))
				.when(col("eventType").equalTo("RECIPE_VIEW"), lit(1))
				.otherwise(lit(0));

		Dataset<Row> scored = events
			.filter(col("eventType").isin(
				"RECIPE_VIEW",
				"RECIPE_BOOKMARKED",
				"RECIPE_UNBOOKMARKED",
				"RECIPE_LIKED",
				"RECIPE_UNLIKED"
			))
			.withColumn("score", scoreCol);

		Dataset<Row> agg = scored
			.withWatermark("eventTime", watermark)
			.groupBy(
				window(col("eventTime"), windowSize, slideSize),
				col("recipeId")
			)
			.agg(sum(col("score")).alias("score"));

		StreamingQuery q = agg.writeStream()
			.outputMode("update") // 누적 점수 update
			.trigger(Trigger.ProcessingTime("60 seconds"))
			.option("checkpointLocation", checkpointDir)
			.foreachBatch((batch, batchId) -> {
				// windowEndMinute 컬럼 추가 + 같은 윈도우끼리 모으기/정렬
				Dataset<Row> keyed = addWindowKey(batch)
					.repartition(col("windowEndMinute"))
					.sortWithinPartitions(col("windowEndMinute"), col("score").desc(), col("recipeId").asc());

				// Redis에 최신 누적 score로 upsert + 항상 topN 유지
				upsertWindowScoresToRedisTopN(keyed, redisHost, redisPort, topN, redisTtlSeconds);

				// (옵션) MySQL 스냅샷
				// writeToMySql(batch, mysqlUrl, mysqlUser, mysqlPass);
			})
			.start();

		q.awaitTermination();
	}

	private static Dataset<Row> addWindowKey(Dataset<Row> df) {
		Column windowEndMinute = date_format(col("window.end"), "yyyy-MM-dd'T'HH:mm");
		return df.withColumn("windowEndMinute", windowEndMinute);
	}

	/**
	 * update 모드에 맞게:
	 * - key: trending:10m:<windowEndMinute> (기존 prefix 유지)
	 * - member: recipeId
	 * - score: 해당 윈도우의 최신 누적 score로 덮어쓰기(ZADD)
	 * - flush 단위: windowEndMinute가 바뀔 때마다
	 * - 항상 topN 유지(명시적으로 zcard 기반 trim)
	 */
	private static void upsertWindowScoresToRedisTopN(
		Dataset<Row> keyedAgg,
		String host,
		int port,
		int topN,
		int ttlSeconds
	) {
		keyedAgg.foreachPartition(it -> {
			try (Jedis jedis = new Jedis(host, port)) {

				String currentKey = null;
				Pipeline p = null;

				while (it.hasNext()) {
					Row r = it.next();

					String windowEndMinute = r.getAs("windowEndMinute");
					String key = "trending:10m:" + windowEndMinute;

					// window key 전환 시: 이전 key flush + trim + expire
					if (currentKey == null || !currentKey.equals(key)) {
						if (p != null) {
							p.sync(); // 먼저 ZADD 반영
							trimTopN(jedis, p, currentKey, topN);
							p.expire(currentKey, ttlSeconds);
							p.sync();
						}
						currentKey = key;
						p = jedis.pipelined();
					}

					long recipeId = ((Number) r.getAs("recipeId")).longValue();
					double score = ((Number) r.getAs("score")).doubleValue();

					// 최신 누적 score로 덮어쓰기 (update 모드 대응)
					p.zadd(currentKey, score, String.valueOf(recipeId));
				}

				// 마지막 key flush + trim + expire
				if (p != null) {
					p.sync();
					trimTopN(jedis, p, currentKey, topN);
					p.expire(currentKey, ttlSeconds);
					p.sync();
				}
			}
		});
	}

	/**
	 * Sorted Set은 score 오름차순 rank 기준으로 0이 최저점.
	 * size > topN이면 "하위(size-topN)개"를 잘라내면 topN개만 남습니다.
	 */
	private static void trimTopN(Jedis jedis, Pipeline p, String key, int topN) {
		Long size = jedis.zcard(key);
		if (size == null) return;

		long s = size;
		if (s > topN) {
			long removeCount = s - topN;
			// 0..removeCount-1 제거 => 하위 점수 제거
			p.zremrangeByRank(key, 0, removeCount - 1);
		}
	}

	// (옵션) MySQL 스냅샷
	private static void writeToMySql(Dataset<Row> batch, String url, String user, String pass) {
		batch.foreachPartition((Iterator<Row> it) -> {
			try (Connection c = DriverManager.getConnection(url, user, pass)) {
				c.setAutoCommit(false);

				String sql = """
                    INSERT INTO recipe_trending_snapshot
                    (window_start, window_end, recipe_id, score, updated_at)
                    VALUES (?, ?, ?, ?, ?)
                    ON DUPLICATE KEY UPDATE
                      score = VALUES(score),
                      updated_at = VALUES(updated_at)
                    """;

				try (PreparedStatement ps = c.prepareStatement(sql)) {
					while (it.hasNext()) {
						Row r = it.next();
						Row w = r.getAs("window");

						Timestamp ws = (Timestamp) w.getAs("start");
						Timestamp we = (Timestamp) w.getAs("end");

						long recipeId = r.getAs("recipeId");
						long score = ((Number) r.getAs("score")).longValue();

						ps.setTimestamp(1, ws);
						ps.setTimestamp(2, we);
						ps.setLong(3, recipeId);
						ps.setLong(4, score);
						ps.setTimestamp(5, Timestamp.from(Instant.now()));
						ps.addBatch();
					}
					ps.executeBatch();
					c.commit();
				} catch (Exception e) {
					c.rollback();
					throw e;
				}
			}
		});
	}

	private static String env(String key, String def) {
		String v = System.getenv(key);
		return (v == null || v.isBlank()) ? def : v;
	}
}