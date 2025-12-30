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
}
