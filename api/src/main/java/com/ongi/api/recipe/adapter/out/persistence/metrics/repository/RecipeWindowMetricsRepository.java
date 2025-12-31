package com.ongi.api.recipe.adapter.out.persistence.metrics.repository;

import com.ongi.api.recipe.adapter.out.persistence.metrics.RecipeWindowMetricsEntity;
import com.ongi.api.recipe.adapter.out.persistence.metrics.RecipeWindowMetricsId;
import java.time.LocalDate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RecipeWindowMetricsRepository extends JpaRepository<RecipeWindowMetricsEntity, RecipeWindowMetricsId> {

	@Modifying
	@Query(value = """
		INSERT INTO recipe_window_metrics (
			as_of_date, window_days, recipe_id,
			view_cnt, engaged_view_cnt, like_net, save_net,
			dwell_ms_sum, scroll_depth_sum
		)
		SELECT
			:asOfDate AS as_of_date,
			:windowDays AS window_days,
			w.recipe_id,
			GREATEST(0, w.view_cnt + COALESCE(d_in.view_cnt,0) - COALESCE(d_out.view_cnt,0)) AS view_cnt,
			GREATEST(0, w.engaged_view_cnt + COALESCE(d_in.engaged_view_cnt,0) - COALESCE(d_out.engaged_view_cnt,0)) AS engaged_view_cnt,
			(w.like_net + COALESCE(d_in.like_cnt,0) - COALESCE(d_in.unlike_cnt,0))
			  - (COALESCE(d_out.like_cnt,0) - COALESCE(d_out.unlike_cnt,0)) AS like_net,
			(w.save_net + COALESCE(d_in.save_cnt,0) - COALESCE(d_in.unsave_cnt,0))
			  - (COALESCE(d_out.save_cnt,0) - COALESCE(d_out.unsave_cnt,0)) AS save_net,
			GREATEST(0, w.dwell_ms_sum + COALESCE(d_in.dwell_ms_sum,0) - COALESCE(d_out.dwell_ms_sum,0)) AS dwell_ms_sum,
			GREATEST(0, w.scroll_depth_sum + COALESCE(d_in.scroll_depth_sum,0) - COALESCE(d_out.scroll_depth_sum,0)) AS scroll_depth_sum
		FROM recipe_window_metrics w
		LEFT JOIN recipe_daily_metrics d_in
			ON d_in.metric_date = :asOfDate AND d_in.recipe_id = w.recipe_id
		LEFT JOIN recipe_daily_metrics d_out
			ON d_out.metric_date = :outDate AND d_out.recipe_id = w.recipe_id
		WHERE w.as_of_date = :prevDate AND w.window_days = :windowDays
		ON DUPLICATE KEY UPDATE
			view_cnt = VALUES(view_cnt),
			engaged_view_cnt = VALUES(engaged_view_cnt),
			like_net = VALUES(like_net),
			save_net = VALUES(save_net),
			dwell_ms_sum = VALUES(dwell_ms_sum),
			scroll_depth_sum = VALUES(scroll_depth_sum),
			modified_at = NOW(6);
	""", nativeQuery = true)
	int upsertIncremental(
		@Param("asOfDate") LocalDate asOfDate,
		@Param("prevDate") LocalDate prevDate,
		@Param("outDate") LocalDate outDate,
		@Param("windowDays") int windowDays
	);

	@Modifying
	@Query(value = """
		INSERT INTO recipe_window_metrics (
			as_of_date, window_days, recipe_id,
			view_cnt, engaged_view_cnt, like_net, save_net,
			dwell_ms_sum, scroll_depth_sum
		)
		SELECT
			:asOfDate, :windowDays, d.recipe_id,
			COALESCE(d.view_cnt,0),
			COALESCE(d.engaged_view_cnt,0),
			(COALESCE(d.like_cnt,0) - COALESCE(d.unlike_cnt,0)),
			(COALESCE(d.save_cnt,0) - COALESCE(d.unsave_cnt,0)),
			COALESCE(d.dwell_ms_sum,0),
			COALESCE(d.scroll_depth_sum,0)
		FROM recipe_daily_metrics d
		LEFT JOIN recipe_window_metrics w
			ON w.as_of_date = :asOfDate AND w.window_days = :windowDays AND w.recipe_id = d.recipe_id
		WHERE d.metric_date = :asOfDate
		  AND w.recipe_id IS NULL
		ON DUPLICATE KEY UPDATE
			modified_at = NOW(6);
	""", nativeQuery = true)
	int seedNewFromDaily(
		@Param("asOfDate") LocalDate asOfDate,
		@Param("windowDays") int windowDays
	);

	@Modifying
	@Query(value = """
		INSERT INTO recipe_window_metrics (
			as_of_date, window_days, recipe_id,
			view_cnt, engaged_view_cnt, like_net, save_net
		)
		SELECT
			:asOfDate, :windowDays, m.recipe_id,
			SUM(m.view_cnt),
			SUM(m.engaged_view_cnt),
			SUM(m.like_cnt) - SUM(m.unlike_cnt),
			SUM(m.save_cnt) - SUM(m.unsave_cnt)
		FROM recipe_daily_metrics m
		WHERE m.metric_date BETWEEN :startDate AND :asOfDate
		GROUP BY m.recipe_id
		ON DUPLICATE KEY UPDATE
			view_cnt = VALUES(view_cnt),
			engaged_view_cnt = VALUES(engaged_view_cnt),
			like_net = VALUES(like_net),
			save_net = VALUES(save_net),
			modified_at = NOW(6);
	""", nativeQuery = true)
	int rebuildWindow(
		@Param("asOfDate") LocalDate asOfDate,
		@Param("windowDays") int windowDays,
		@Param("startDate") LocalDate startDate
	);
}
