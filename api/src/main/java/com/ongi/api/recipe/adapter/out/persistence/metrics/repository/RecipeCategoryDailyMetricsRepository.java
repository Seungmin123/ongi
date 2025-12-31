package com.ongi.api.recipe.adapter.out.persistence.metrics.repository;

import com.ongi.api.recipe.adapter.out.persistence.metrics.RecipeCategoryDailyMetricsEntity;
import com.ongi.api.recipe.adapter.out.persistence.metrics.RecipeCategoryDailyMetricsId;
import java.time.LocalDate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface RecipeCategoryDailyMetricsRepository extends JpaRepository<RecipeCategoryDailyMetricsEntity, RecipeCategoryDailyMetricsId> {

	// ---------------------------
	// view
	// ---------------------------

	@Modifying
	@Query(value = """
		INSERT INTO recipe_category_daily_metrics (metric_date, category, view_cnt)
		VALUES (:metricDate, :category, 1)
		ON DUPLICATE KEY UPDATE
		  view_cnt = view_cnt + 1,
		  modified_at = NOW(6)
	""", nativeQuery = true)
	int upsertView(@Param("metricDate") LocalDate metricDate, @Param("category") String category);

	// ---------------------------
	// like / unlike
	// ---------------------------

	@Modifying
	@Query(value = """
		INSERT INTO recipe_category_daily_metrics (metric_date, category, like_cnt)
		VALUES (:metricDate, :category, 1)
		ON DUPLICATE KEY UPDATE
		  like_cnt = like_cnt + 1,
		  modified_at = NOW(6)
	""", nativeQuery = true)
	int upsertLike(@Param("metricDate") LocalDate metricDate, @Param("category") String category);

	@Modifying
	@Query(value = """
			INSERT INTO recipe_category_daily_metrics (metric_date, category, unlike_cnt)
			VALUES (:metricDate, :category, 1)
			ON DUPLICATE KEY UPDATE
			  unlike_cnt = unlike_cnt + 1,
			  modified_at = NOW(6)
		""", nativeQuery = true)
	int upsertUnlike(@Param("metricDate") LocalDate metricDate, @Param("category") String category);

	// ---------------------------
	// save / unsave
	// ---------------------------

	@Modifying
	@Query(value = """
			INSERT INTO recipe_category_daily_metrics (metric_date, category, save_cnt)
			VALUES (:metricDate, :category, 1)
			ON DUPLICATE KEY UPDATE
			  save_cnt = save_cnt + 1,
			  modified_at = NOW(6)
		""", nativeQuery = true)
	int upsertSave(@Param("metricDate") LocalDate metricDate, @Param("category") String category);

	@Modifying
	@Query(value = """
			INSERT INTO recipe_category_daily_metrics (metric_date, category, unsave_cnt)
			VALUES (:metricDate, :category, 1)
			ON DUPLICATE KEY UPDATE
			  unsave_cnt = unsave_cnt + 1,
			  modified_at = NOW(6)
		""", nativeQuery = true)
	int upsertUnsave(@Param("metricDate") LocalDate metricDate, @Param("category") String category);

}
