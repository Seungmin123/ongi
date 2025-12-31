package com.ongi.api.ingredients.adapter.out.persistence.repository;

import com.ongi.api.ingredients.adapter.out.persistence.projection.RelatedRecipeRow;
import jakarta.persistence.EntityManager;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@Repository
public class RecipeRelatedNativeRepository {

	private final EntityManager em;

	public List<RelatedRecipeRow> findRelatedWithPopularityBoost(
		Long recipeId,
		int limit,
		double categoryAlpha,
		double idfBase,
		double minScore,
		int minOverlapCount,
		double minCenteredScore,
		// seed
		int seedK,
		double seedMinIdf,
		int seedMinHit,
		double seedAlpha,
		// popularity
		double popBeta
	) {
		String sql = """
			WITH seeds AS (
				SELECT
					ri.ingredient_id,
					idf.idf
				FROM recipe_ingredient ri
				JOIN ingredient_idf idf
					ON idf.ingredient_id = ri.ingredient_id
				WHERE ri.recipe_id = :recipeId
					AND (:seedMinIdf = 0 OR idf.idf >= :seedMinIdf)
				ORDER BY idf.idf DESC, ri.ingredient_id DESC
				LIMIT %d
			)
			SELECT
				ri2.recipe_id AS recipe_id,
				SUM(idf.idf) AS raw_sum_idf,
				SUM(GREATEST(0, idf.idf - :idfBase)) AS centered_sum_idf,
				COUNT(DISTINCT ri1.ingredient_id) AS overlap_cnt,
				-- seed가 몇 개 겹쳤는지 (0~seedK)
				COUNT(DISTINCT s.ingredient_id) AS seed_hit_cnt,
				-- base score (연관성)
				(
					SUM(GREATEST(0, idf.idf - :idfBase))
					+ CASE
						WHEN r1.category IS NOT NULL
							AND r2.category IS NOT NULL
							AND r1.category = r2.category
						THEN :categoryAlpha
						ELSE 0
					END
					+ (:seedAlpha * COUNT(DISTINCT s.ingredient_id))
				) AS score,
				COALESCE(m.view_cnt, 0) AS view_7d,
				-- final score (안정적으로: popularity는 가산항)
				(
					(
						SUM(GREATEST(0, idf.idf - :idfBase))
						+ CASE
							WHEN r1.category IS NOT NULL
								AND r2.category IS NOT NULL
								AND r1.category = r2.category
							THEN :categoryAlpha
							ELSE 0
						END
						+ (:seedAlpha * COUNT(DISTINCT s.ingredient_id))
					)
					+ (:popBeta * LOG(1 + COALESCE(m.view_cnt, 0)))
				) AS final_score
			FROM recipe_ingredient ri1
			JOIN ingredient_idf idf
				ON idf.ingredient_id = ri1.ingredient_id
			JOIN recipe_ingredient ri2
				ON ri2.ingredient_id = ri1.ingredient_id
					AND ri2.recipe_id <> :recipeId
			-- seed hit 계산용 조인(겹치면 잡힘)
			LEFT JOIN seeds s
				ON s.ingredient_id = ri1.ingredient_id
			JOIN recipe r1
				ON r1.recipe_id = :recipeId
			JOIN recipe r2
				ON r2.recipe_id = ri2.recipe_id
			LEFT JOIN recipe_window_metrics m
				ON m.recipe_id = ri2.recipe_id
					AND m.as_of_date = CURDATE()
					AND m.window_days = 7
			WHERE ri1.recipe_id = :recipeId
			GROUP BY ri2.recipe_id, r1.category, r2.category, m.view_cnt
			HAVING
				COUNT(DISTINCT ri1.ingredient_id) >= :minOverlapCount
					AND SUM(idf.idf) >= :minScore
					-- centered 컷(옵션)
					AND (
						:minCenteredScore = 0
						OR SUM(GREATEST(0, idf.idf - :idfBase)) >= :minCenteredScore
					)
					-- seed 조건(coverage 안정)
					AND COUNT(DISTINCT s.ingredient_id) >= :seedMinHit
			ORDER BY final_score DESC, ri2.recipe_id DESC
			LIMIT %d
			""".formatted(seedK, limit);

		@SuppressWarnings("unchecked")
		List<Object[]> rows = em.createNativeQuery(sql)
			.setParameter("recipeId", recipeId)
			.setParameter("categoryAlpha", categoryAlpha)
			.setParameter("idfBase", idfBase)
			.setParameter("minScore", minScore)
			.setParameter("minOverlapCount", minOverlapCount)
			.setParameter("minCenteredScore", minCenteredScore)
			.setParameter("seedMinIdf", seedMinIdf)
			.setParameter("seedMinHit", seedMinHit)
			.setParameter("seedAlpha", seedAlpha)
			.setParameter("popBeta", popBeta)
			.getResultList();

		return rows.stream()
			.map(r -> new RelatedRecipeRow(
				((Number) r[0]).longValue(),   // recipe_id
				((Number) r[1]).doubleValue(), // raw_sum_idf
				((Number) r[2]).doubleValue(), // centeredSumIdf
				((Number) r[3]).intValue(),    // overlap_cnt
				((Number) r[4]).intValue(),    // rare_overlap_cnt
				((Number) r[5]).doubleValue(),  // score
				((Number) r[6]).longValue(),  // score
				((Number) r[7]).doubleValue()  // score
			))
			.toList();
	}
}
