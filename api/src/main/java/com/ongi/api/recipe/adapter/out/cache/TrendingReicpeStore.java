package com.ongi.api.recipe.adapter.out.cache;

import java.time.Clock;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.data.redis.core.ZSetOperations.TypedTuple;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class TrendingReicpeStore {

	// Spark와 동일하게 맞출 것.
	private final ZoneId SPARK_ZONE = ZoneId.of("UTC");
	private static final ZoneId KST = ZoneId.of("Asia/Seoul");
	private final Clock clock = Clock.systemUTC();

	private final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");

	private final StringRedisTemplate redisTemplate;

	public List<Long> getMergedTopRecipeIds(int limit) {
		List<String> keys = computeKeys(clock, KST);

		// merge 정확도를 위해 limit보다 넉넉히 뽑아오기 (2배~5배)
		int fetch = Math.max(limit * 5, 200);

		Map<String, Double> scoreSum = new HashMap<>();

		ZSetOperations<String, String> z = redisTemplate.opsForZSet();

		for (String key : keys) {
			Set<TypedTuple<String>> tuples = z.reverseRangeWithScores(key, 0, fetch - 1);
			if (tuples == null) continue;

			for (ZSetOperations.TypedTuple<String> t : tuples) {
				if (t.getValue() == null || t.getScore() == null) continue;
				scoreSum.merge(t.getValue(), t.getScore(), Double::sum); // 두 윈도우 점수 합산
			}
		}

		return scoreSum.entrySet().stream()
			.sorted((a, b) -> Double.compare(b.getValue(), a.getValue()))
			.limit(limit)
			.map(e -> Long.valueOf(e.getKey()))
			.collect(Collectors.toList());
	}

	public List<String> computeKeys(Clock clock, ZoneId zoneId) {
		ZonedDateTime now = ZonedDateTime.now(clock).withZoneSameInstant(zoneId);

		// 1분 단위로 "올림"
		ZonedDateTime end1 = ceilToMinute(now);
		ZonedDateTime end2 = end1.minusMinutes(1);

		return List.of(
			"trending:10m:" + end1.format(FMT),
			"trending:10m:" + end2.format(FMT)
		);
	}

	private ZonedDateTime ceilToMinute(ZonedDateTime t) {
		ZonedDateTime truncated = t.truncatedTo(ChronoUnit.MINUTES);
		if (t.equals(truncated)) return t;
		return truncated.plusMinutes(1);
	}
}
