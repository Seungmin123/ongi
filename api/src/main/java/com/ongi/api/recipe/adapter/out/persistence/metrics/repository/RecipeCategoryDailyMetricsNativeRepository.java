package com.ongi.api.recipe.adapter.out.persistence.metrics.repository;

import jakarta.persistence.EntityManager;
import java.sql.Date;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@Repository
public class RecipeCategoryDailyMetricsNativeRepository {

	private final EntityManager em;

	/**
	 * view_cnt + 1
	 */
	public int upsertView(LocalDate metricDate, String category) {
		String sql = """
			INSERT INTO recipe_category_daily_metrics (metric_date, category, view_cnt)
			VALUES (?, ?, 1)
			ON DUPLICATE KEY UPDATE
			  view_cnt = view_cnt + 1,
			  modified_at = NOW(6)
		""";

		return em.createNativeQuery(sql)
			.setParameter(1, Date.valueOf(metricDate))
			.setParameter(2, category)
			.executeUpdate();
	}

	// ---------------------------
	// like/save upsert
	// ---------------------------

	public int upsertLike(LocalDate metricDate, String category) {
		String sql = """
			INSERT INTO recipe_category_daily_metrics (metric_date, category, like_cnt)
			VALUES (?, ?, 1)
			ON DUPLICATE KEY UPDATE
			  like_cnt = like_cnt + 1,
			  modified_at = NOW(6)
		""";
		return em.createNativeQuery(sql)
			.setParameter(1, Date.valueOf(metricDate))
			.setParameter(2, category)
			.executeUpdate();
	}

	public int upsertUnlike(LocalDate metricDate, String category) {
		String sql = """
			INSERT INTO recipe_category_daily_metrics (metric_date, category, unlike_cnt)
			VALUES (?, ?, 1)
			ON DUPLICATE KEY UPDATE
			  unlike_cnt = unlike_cnt + 1,
			  modified_at = NOW(6)
		""";
		return em.createNativeQuery(sql)
			.setParameter(1, Date.valueOf(metricDate))
			.setParameter(2, category)
			.executeUpdate();
	}

	public int upsertSave(LocalDate metricDate, String category) {
		String sql = """
			INSERT INTO recipe_category_daily_metrics (metric_date, category, save_cnt)
			VALUES (?, ?, 1)
			ON DUPLICATE KEY UPDATE
			  save_cnt = save_cnt + 1,
			  modified_at = NOW(6)
		""";
		return em.createNativeQuery(sql)
			.setParameter(1, Date.valueOf(metricDate))
			.setParameter(2, category)
			.executeUpdate();
	}

	public int upsertUnsave(LocalDate metricDate, String category) {
		String sql = """
			INSERT INTO recipe_category_daily_metrics (metric_date, category, unsave_cnt)
			VALUES (?, ?, 1)
			ON DUPLICATE KEY UPDATE
			  unsave_cnt = unsave_cnt + 1,
			  modified_at = NOW(6)
		""";
		return em.createNativeQuery(sql)
			.setParameter(1, Date.valueOf(metricDate))
			.setParameter(2, category)
			.executeUpdate();
	}

}
