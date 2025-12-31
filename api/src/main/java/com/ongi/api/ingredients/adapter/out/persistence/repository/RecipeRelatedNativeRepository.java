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

	/**
	 * 기준 레시피의 ingredient들을 기준으로
	 * 공유하는 다른 레시피들을 모아 (centered idf + category alpha) 점수로 정렬
	 *
	 * - minRareOverlapCount == 0 이면 rare 컷 OFF
	 * - minCenteredScore == 0 이면 centered 컷 OFF
	 */
	public List<RelatedRecipeRow> findRelatedByIdfNative(
		Long recipeId,
		int limit,
		double categoryAlpha,
		double idfBase,
		double rareMinIdf,
		double minScore,            // raw_sum_idf 기준 하한
		int minOverlapCount,
		int minRareOverlapCount,
		double minCenteredScore     // centered_sum_idf 기준 하한(옵션)
	) {
		String sql = """
			SELECT
				ri2.recipe_id AS recipe_id,
				SUM(idf.idf) AS raw_sum_idf,
				SUM(GREATEST(0, idf.idf - :idfBase)) AS centered_sum_idf,
				COUNT(DISTINCT ri1.ingredient_id) AS overlap_cnt,
				SUM(CASE WHEN idf.idf >= :rareMinIdf THEN 1 ELSE 0 END) AS rare_overlap_cnt,
				(
				  SUM(GREATEST(0, idf.idf - :idfBase))
				  + CASE
				      WHEN r1.category IS NOT NULL
				       AND r2.category IS NOT NULL
				       AND r1.category = r2.category
				      THEN :categoryAlpha
				      ELSE 0
				    END
				) AS score
			FROM recipe_ingredient ri1
			JOIN ingredient_idf idf
				ON idf.ingredient_id = ri1.ingredient_id
			JOIN recipe_ingredient ri2
				ON ri2.ingredient_id = ri1.ingredient_id
					AND ri2.recipe_id <> :recipeId
			JOIN recipe r1
				ON r1.recipe_id = :recipeId
			JOIN recipe r2
				ON r2.recipe_id = ri2.recipe_id
			WHERE ri1.recipe_id = :recipeId
			GROUP BY ri2.recipe_id, r1.category, r2.category
			HAVING
			COUNT(DISTINCT ri1.ingredient_id) >= :minOverlapCount
				AND SUM(idf.idf) >= :minScore
				-- centered 컷(옵션)
				AND (
				  :minCenteredScore = 0
				  OR SUM(GREATEST(0, idf.idf - :idfBase)) >= :minCenteredScore
				)
				-- rare 컷(옵션)
				AND (
				  :minRareOverlapCount = 0
				  OR SUM(CASE WHEN idf.idf >= :rareMinIdf THEN 1 ELSE 0 END) >= :minRareOverlapCount
				)
			ORDER BY score DESC, ri2.recipe_id DESC
			LIMIT %d
		""".formatted(limit);

		@SuppressWarnings("unchecked")
		List<Object[]> rows = em.createNativeQuery(sql)
			.setParameter("recipeId", recipeId)
			.setParameter("categoryAlpha", categoryAlpha)
			.setParameter("idfBase", idfBase)
			.setParameter("rareMinIdf", rareMinIdf)
			.setParameter("minScore", minScore)
			.setParameter("minOverlapCount", minOverlapCount)
			.setParameter("minRareOverlapCount", minRareOverlapCount)
			.setParameter("minCenteredScore", minCenteredScore)
			.getResultList();

		return rows.stream()
			.map(r -> new RelatedRecipeRow(
				((Number) r[0]).longValue(),   // recipe_id
				((Number) r[1]).doubleValue(), // raw_sum_idf
				((Number) r[3]).intValue(),    // overlap_cnt
				((Number) r[4]).intValue(),    // rare_overlap_cnt
				((Number) r[5]).doubleValue()  // score
			))
			.toList();
	}

	public List<RelatedRecipeRow> findRelatedWithPopularityBoost(
		Long recipeId,
		int limit,
		double categoryAlpha,
		double idfBase,
		double rareMinIdf,
		double minScore,            // raw_sum_idf 기준 하한
		int minOverlapCount,
		int minRareOverlapCount,
		double minCenteredScore     // centered_sum_idf 기준 하한(옵션)
	) {
		String sql = """
			SELECT
				ri2.recipe_id AS recipe_id,
				SUM(idf.idf) AS raw_sum_idf,
				SUM(GREATEST(0, idf.idf - :idfBase)) AS centered_sum_idf,
				COUNT(DISTINCT ri1.ingredient_id) AS overlap_cnt,
				SUM(CASE WHEN idf.idf >= :rareMinIdf THEN 1 ELSE 0 END) AS rare_overlap_cnt,
				(
				  SUM(GREATEST(0, idf.idf - :idfBase))
				  + CASE
				      WHEN r1.category IS NOT NULL
				       AND r2.category IS NOT NULL
				       AND r1.category = r2.category
				      THEN :categoryAlpha
				      ELSE 0
				    END
				) AS score,
				COALESCE(m.view_cnt, 0) AS view_7d,
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
				  ) * LOG(1 + COALESCE(m.view_cnt, 0))
				) AS final_score
			FROM recipe_ingredient ri1
			JOIN ingredient_idf idf
				ON idf.ingredient_id = ri1.ingredient_id
			JOIN recipe_ingredient ri2
				ON ri2.ingredient_id = ri1.ingredient_id
					AND ri2.recipe_id <> :recipeId
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
				AND (
				  :minCenteredScore = 0
				  OR SUM(GREATEST(0, idf.idf - :idfBase)) >= :minCenteredScore
				)
				AND (
				  :minRareOverlapCount = 0
				  OR SUM(CASE WHEN idf.idf >= :rareMinIdf THEN 1 ELSE 0 END) >= :minRareOverlapCount
				)
			ORDER BY final_score DESC, ri2.recipe_id DESC
			LIMIT %d
		""".formatted(limit);

		@SuppressWarnings("unchecked")
		List<Object[]> rows = em.createNativeQuery(sql)
			.setParameter("recipeId", recipeId)
			.setParameter("categoryAlpha", categoryAlpha)
			.setParameter("idfBase", idfBase)
			.setParameter("rareMinIdf", rareMinIdf)
			.setParameter("minScore", minScore)
			.setParameter("minOverlapCount", minOverlapCount)
			.setParameter("minRareOverlapCount", minRareOverlapCount)
			.setParameter("minCenteredScore", minCenteredScore)
			.getResultList();

		return rows.stream()
			.map(r -> new RelatedRecipeRow(
				((Number) r[0]).longValue(),   // recipe_id
				((Number) r[1]).doubleValue(), // raw_sum_idf
				((Number) r[3]).intValue(),    // overlap_cnt
				((Number) r[4]).intValue(),    // rare_overlap_cnt
				((Number) r[5]).doubleValue()  // score
			))
			.toList();
	}
}
