package com.ongi.api.recipe.adapter.out.persistence.metrics.repository;

import com.ongi.api.recipe.adapter.out.persistence.metrics.projection.RecipeView7dRow;
import jakarta.persistence.EntityManager;
import java.sql.Date;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@Repository
public class RecipeDailyMetricsNativeRepository {

	private final EntityManager em;

	/**
	 * view_cnt + 1
	 */
	public int upsertView(LocalDate metricDate, long recipeId) {
		String sql = """
			INSERT INTO recipe_daily_metrics (metric_date, recipe_id, view_cnt)
			VALUES (?, ?, 1)
			ON DUPLICATE KEY UPDATE
			  view_cnt = view_cnt + 1,
			  updated_at = NOW(6)
		""";

		return em.createNativeQuery(sql)
			.setParameter(1, Date.valueOf(metricDate))
			.setParameter(2, recipeId)
			.executeUpdate();
	}

	/**
	 * engaged_view_cnt + 1 and engagement aggregates
	 *
	 * scrollDepth: 0~100
	 */
	public int upsertEngagedView(
		LocalDate metricDate,
		long recipeId,
		long dwellMs,
		int scrollDepth
	) {
		String sql = """
			INSERT INTO recipe_daily_metrics (
			  metric_date, recipe_id,
			  engaged_view_cnt,
			  dwell_ms_sum, dwell_ms_max,
			  scroll_depth_sum, scroll_depth_max,
			  dwell_ge_3s_cnt, dwell_ge_10s_cnt,
			  scroll_ge_50_cnt, scroll_ge_90_cnt
			)
			VALUES (
			  ?, ?,
			  1,
			  ?, ?,
			  ?, ?,
			  IF(? >= 3000, 1, 0),
			  IF(? >= 10000, 1, 0),
			  IF(? >= 50, 1, 0),
			  IF(? >= 90, 1, 0)
			)
			ON DUPLICATE KEY UPDATE
			  engaged_view_cnt = engaged_view_cnt + 1,
			  dwell_ms_sum = dwell_ms_sum + VALUES(dwell_ms_sum),
			  dwell_ms_max = GREATEST(dwell_ms_max, VALUES(dwell_ms_max)),
			  scroll_depth_sum = scroll_depth_sum + VALUES(scroll_depth_sum),
			  scroll_depth_max = GREATEST(scroll_depth_max, VALUES(scroll_depth_max)),
			  dwell_ge_3s_cnt = dwell_ge_3s_cnt + VALUES(dwell_ge_3s_cnt),
			  dwell_ge_10s_cnt = dwell_ge_10s_cnt + VALUES(dwell_ge_10s_cnt),
			  scroll_ge_50_cnt = scroll_ge_50_cnt + VALUES(scroll_ge_50_cnt),
			  scroll_ge_90_cnt = scroll_ge_90_cnt + VALUES(scroll_ge_90_cnt),
			  updated_at = NOW(6)
		""";

		return em.createNativeQuery(sql)
			.setParameter(1, Date.valueOf(metricDate))
			.setParameter(2, recipeId)
			.setParameter(3, dwellMs)
			.setParameter(4, (int) dwellMs)
			.setParameter(5, scrollDepth)
			.setParameter(6, scrollDepth)
			.setParameter(7, dwellMs)
			.setParameter(8, dwellMs)
			.setParameter(9, scrollDepth)
			.setParameter(10, scrollDepth)
			.executeUpdate();
	}

	/**
	 * 최근 7일 view 합 (테이블 전체 스캔 방지 위해 후보 recipeIds로 IN 필터링 권장)
	 *
	 * metric_date >= CURDATE() - INTERVAL 6 DAY
	 */
	@SuppressWarnings("unchecked")
	public List<RecipeView7dRow> findView7dByRecipeIds(List<Long> recipeIds) {
		if (recipeIds == null || recipeIds.isEmpty()) return List.of();

		String sql = """
			SELECT recipe_id, SUM(view_cnt) AS view_7d
			FROM recipe_daily_metrics
			WHERE metric_date >= CURDATE() - INTERVAL 6 DAY
			  AND recipe_id IN (:ids)
			GROUP BY recipe_id
		""";

		List<Object[]> rows = em.createNativeQuery(sql)
			.setParameter("ids", recipeIds)
			.getResultList();

		return rows.stream()
			.map(r -> new RecipeView7dRow(
				((Number) r[0]).longValue(),
				((Number) r[1]).longValue()
			))
			.toList();
	}

	// ---------------------------
	// like/save upsert
	// ---------------------------

	public int upsertLike(LocalDate metricDate, long recipeId) {
		String sql = """
			INSERT INTO recipe_daily_metrics (metric_date, recipe_id, like_cnt)
			VALUES (?, ?, 1)
			ON DUPLICATE KEY UPDATE
			  like_cnt = like_cnt + 1,
			  updated_at = NOW(6)
		""";
		return em.createNativeQuery(sql)
			.setParameter(1, Date.valueOf(metricDate))
			.setParameter(2, recipeId)
			.executeUpdate();
	}

	public int upsertUnlike(LocalDate metricDate, long recipeId) {
		String sql = """
			INSERT INTO recipe_daily_metrics (metric_date, recipe_id, unlike_cnt)
			VALUES (?, ?, 1)
			ON DUPLICATE KEY UPDATE
			  unlike_cnt = unlike_cnt + 1,
			  updated_at = NOW(6)
		""";
		return em.createNativeQuery(sql)
			.setParameter(1, Date.valueOf(metricDate))
			.setParameter(2, recipeId)
			.executeUpdate();
	}

	public int upsertSave(LocalDate metricDate, long recipeId) {
		String sql = """
			INSERT INTO recipe_daily_metrics (metric_date, recipe_id, save_cnt)
			VALUES (?, ?, 1)
			ON DUPLICATE KEY UPDATE
			  save_cnt = save_cnt + 1,
			  updated_at = NOW(6)
		""";
		return em.createNativeQuery(sql)
			.setParameter(1, Date.valueOf(metricDate))
			.setParameter(2, recipeId)
			.executeUpdate();
	}

	public int upsertUnsave(LocalDate metricDate, long recipeId) {
		String sql = """
			INSERT INTO recipe_daily_metrics (metric_date, recipe_id, unsave_cnt)
			VALUES (?, ?, 1)
			ON DUPLICATE KEY UPDATE
			  unsave_cnt = unsave_cnt + 1,
			  updated_at = NOW(6)
		""";
		return em.createNativeQuery(sql)
			.setParameter(1, Date.valueOf(metricDate))
			.setParameter(2, recipeId)
			.executeUpdate();
	}
}
