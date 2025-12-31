package com.ongi.api.recipe.adapter.out.persistence.metrics.repository;

import com.ongi.api.recipe.adapter.out.persistence.metrics.projection.RecipeView7dRow;
import com.ongi.api.recipe.adapter.out.persistence.metrics.RecipeDailyMetricsEntity;
import com.ongi.api.recipe.adapter.out.persistence.metrics.RecipeDailyMetricsId;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RecipeDailyMetricsRepository extends JpaRepository<RecipeDailyMetricsEntity, RecipeDailyMetricsId> {

	// ---------------------------
	// view upsert
	// ---------------------------
	@Modifying
	@Query(value = """
		INSERT INTO recipe_daily_metrics (metric_date, recipe_id, view_cnt)
		VALUES (:metricDate, :recipeId, 1)
		ON DUPLICATE KEY UPDATE
		  view_cnt = view_cnt + 1,
		  modified_at = NOW(6)
	""", nativeQuery = true)
	int upsertView(@Param("metricDate") LocalDate metricDate, @Param("recipeId") long recipeId);

	// ---------------------------
	// engaged view upsert
	// ---------------------------
	@Modifying
	@Query(value = """
		INSERT INTO recipe_daily_metrics (
		  metric_date, recipe_id,
		  engaged_view_cnt,
		  dwell_ms_sum, dwell_ms_max,
		  scroll_depth_sum, scroll_depth_max,
		  dwell_ge_3s_cnt, dwell_ge_10s_cnt,
		  scroll_ge_50_cnt, scroll_ge_90_cnt
		)
		VALUES (
		  :metricDate, :recipeId,
		  1,
		  :dwellMs, :dwellMs,
		  :scrollDepth, :scrollDepth,
		  IF(:dwellMs >= 3000, 1, 0),
		  IF(:dwellMs >= 10000, 1, 0),
		  IF(:scrollDepth >= 50, 1, 0),
		  IF(:scrollDepth >= 90, 1, 0)
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
		  modified_at = NOW(6)
	""", nativeQuery = true)
	int upsertEngagedView(@Param("metricDate") LocalDate metricDate, @Param("recipeId") long recipeId, @Param("dwellMs") long dwellMs, @Param("scrollDepth") int scrollDepth);

	// ---------------------------
	// like/save upsert
	// ---------------------------
	@Modifying
	@Query(value = """
		INSERT INTO recipe_daily_metrics (metric_date, recipe_id, like_cnt)
		VALUES (:metricDate, :recipeId, 1)
		ON DUPLICATE KEY UPDATE
		  like_cnt = like_cnt + 1,
		  modified_at = NOW(6)
	""", nativeQuery = true)
	int upsertLike(@Param("metricDate") LocalDate metricDate, @Param("recipeId") long recipeId);

	@Modifying
	@Query(value = """
		INSERT INTO recipe_daily_metrics (metric_date, recipe_id, unlike_cnt)
		VALUES (:metricDate, :recipeId, 1)
		ON DUPLICATE KEY UPDATE
		  unlike_cnt = unlike_cnt + 1,
		  modified_at = NOW(6)
	""", nativeQuery = true)
	int upsertUnlike(@Param("metricDate") LocalDate metricDate, @Param("recipeId") long recipeId);

	@Modifying
	@Query(value = """
		INSERT INTO recipe_daily_metrics (metric_date, recipe_id, save_cnt)
		VALUES (:metricDate, :recipeId, 1)
		ON DUPLICATE KEY UPDATE
		  save_cnt = save_cnt + 1,
		  modified_at = NOW(6)
	""", nativeQuery = true)
	int upsertSave(@Param("metricDate") LocalDate metricDate, @Param("recipeId") long recipeId);

	@Modifying
	@Query(value = """
		INSERT INTO recipe_daily_metrics (metric_date, recipe_id, unsave_cnt)
		VALUES (:metricDate, :recipeId, 1)
		ON DUPLICATE KEY UPDATE
		  unsave_cnt = unsave_cnt + 1,
		  modified_at = NOW(6)
	""", nativeQuery = true)
	int upsertUnsave(@Param("metricDate") LocalDate metricDate, @Param("recipeId") long recipeId);

	// ---------------------------
	// view_7d query
	// ---------------------------
	@Query(value = """
		SELECT m.recipe_id AS recipeId, SUM(m.view_cnt) AS view7d
		FROM recipe_daily_metrics m
		WHERE m.metric_date >= CURDATE() - INTERVAL 6 DAY
		  AND m.recipe_id IN (:ids)
		GROUP BY m.recipe_id
	""", nativeQuery = true)
	List<RecipeView7dRow> findView7dByRecipeIds(@Param("ids") List<Long> ids);
}