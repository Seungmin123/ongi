package com.ongi.api.ingredients.batch.application;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class IngredientIdfBatchService {

	private final EntityManager em;

	@Transactional(transactionManager = "transactionManager")
	public void recomputeIdf() {
		// 값이 클 수록 희귀, 작을수록 흔함


		// 1) N 계산 (recipe table)
		Long n = ((Number) em.createNativeQuery("SELECT COUNT(*) FROM recipe").getSingleResult()).longValue();

		// 2) ingredient_idf 갱신 (upsert)
		// df: recipe_ingredient에서 ingredient별 distinct recipe 개수
		String sql = """
			INSERT INTO ingredient_idf (ingredient_id, df, idf, computed_at)
			SELECT
				ri.ingredient_id AS ingredient_id,
				COUNT(DISTINCT ri.recipe_id) AS df,
				(LOG((:n + 1) / (COUNT(DISTINCT ri.recipe_id) + 1)) + 1) AS idf,
				NOW() AS computed_at
			FROM recipe_ingredient ri
			GROUP BY ri.ingredient_id
			ON DUPLICATE KEY UPDATE
				df = VALUES(df),
				idf = VALUES(idf),
				computed_at = VALUES(computed_at)
		""";

		em.createNativeQuery(sql)
			.setParameter("n", n.doubleValue())
			.executeUpdate();
	}

	public void refreshIdfQuantiles() {
		String sql = """
			UPDATE recipe_related_config c
			JOIN (
			 WITH ranked AS (
			   SELECT
			     idf,
			     ROW_NUMBER() OVER (ORDER BY idf) AS rn,
			     COUNT(*) OVER () AS cnt
			   FROM ingredient_idf
			   WHERE idf IS NOT NULL
			 )
			 SELECT
			   MIN(idf) AS min_idf,
			   MAX(idf) AS max_idf,
			   AVG(idf) AS avg_idf,
			   MAX(CASE WHEN rn >= CEIL(cnt * 0.10) THEN idf END) AS p10,
			   MAX(CASE WHEN rn >= CEIL(cnt * 0.25) THEN idf END) AS p25,
			   MAX(CASE WHEN rn >= CEIL(cnt * 0.50) THEN idf END) AS p50,
			   MAX(CASE WHEN rn >= CEIL(cnt * 0.75) THEN idf END) AS p75,
			   MAX(CASE WHEN rn >= CEIL(cnt * 0.90) THEN idf END) AS p90
			 FROM ranked
			) x
			SET
			 -- centered base는 보통 p25~p50 사이에서 시작하는 게 안정적.
			 c.idf_base = x.p25,
			 -- 희귀 기준은 p50은 너무 낮을 수 있어서, 실제 운영에선 p75~p90도 후보로.
			 c.rare_min_idf = x.p50,
			 c.updated_at = NOW(6)
			WHERE c.config_id = 1;
		""";

		em.createNativeQuery(sql).executeUpdate();

		String centeredScoreSql = """
			UPDATE recipe_related_config c
			JOIN (
			WITH
			sampled_recipe AS (
			 SELECT recipe_id
			 FROM recipe
			 ORDER BY RAND()
			 LIMIT 200
			),
			-- 1) (기준레시피 s, 후보레시피 ri2.recipe_id) 단위로 centered_sum 계산
			candidate_scores AS (
			 SELECT
			   s.recipe_id AS base_recipe_id,
			   ri2.recipe_id AS cand_recipe_id,
			   SUM(GREATEST(0, idf.idf - c2.idf_base)) AS centered_sum
			 FROM sampled_recipe s
			 JOIN recipe_related_config c2 ON c2.config_id = 1
			 JOIN recipe_ingredient ri1 ON ri1.recipe_id = s.recipe_id
			 JOIN ingredient_idf idf ON idf.ingredient_id = ri1.ingredient_id
			 JOIN recipe_ingredient ri2
			   ON ri2.ingredient_id = ri1.ingredient_id
			  AND ri2.recipe_id <> s.recipe_id
			 GROUP BY s.recipe_id, ri2.recipe_id
			),
			-- 2) 기준레시피별로 후보들 중 centered_sum 최대값만 뽑기
			best_centered AS (
			 SELECT
			   base_recipe_id,
			   MAX(centered_sum) AS best_centered_sum
			 FROM candidate_scores
			 GROUP BY base_recipe_id
			),
			ranked AS (
			 SELECT
			   best_centered_sum AS v,
			   ROW_NUMBER() OVER (ORDER BY best_centered_sum) AS rn,
			   COUNT(*) OVER () AS cnt
			 FROM best_centered
			 WHERE best_centered_sum IS NOT NULL
			)
			SELECT
			 MAX(CASE WHEN rn >= CEIL(cnt * 0.25) THEN v END) AS p25_best,
			 MAX(CASE WHEN rn >= CEIL(cnt * 0.50) THEN v END) AS p50_best
			FROM ranked
			) x
			SET
			c.min_centered_score = x.p25_best,
			c.updated_at = NOW(6)
			WHERE c.config_id = 1;
		""";

		em.createNativeQuery(centeredScoreSql).executeUpdate();
	}
}
