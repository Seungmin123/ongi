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
	 * 공유하는 다른 레시피들을 모아 ingredient_idf 합산 점수로 정렬
	 */
	public List<RelatedRecipeRow> findRelatedByIdfNative(Long recipeId, int limit) {
		String sql = """
			SELECT
				ri2.recipe_id AS recipe_id,
				SUM(idf.idf)  AS score
			FROM recipe_ingredient ri1
			JOIN ingredient_idf idf
				ON idf.ingredient_id = ri1.ingredient_id
			JOIN recipe_ingredient ri2
				ON ri2.ingredient_id = ri1.ingredient_id
					AND ri2.recipe_id <> :recipeId
			WHERE ri1.recipe_id = :recipeId
			GROUP BY ri2.recipe_id
			ORDER BY score DESC, ri2.recipe_id DESC
			LIMIT %d
		""".formatted(limit);

		@SuppressWarnings("unchecked")
		List<Object[]> rows = em.createNativeQuery(sql)
			.setParameter("recipeId", recipeId)
			.getResultList();

		return rows.stream()
			.map(r -> new RelatedRecipeRow(((Number) r[0]).longValue(), ((Number) r[1]).doubleValue()))
			.toList();
	}
}
