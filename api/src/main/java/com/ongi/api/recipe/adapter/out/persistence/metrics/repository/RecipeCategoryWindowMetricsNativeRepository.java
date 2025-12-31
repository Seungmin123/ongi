package com.ongi.api.recipe.adapter.out.persistence.metrics.repository;

import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@Repository
public class RecipeCategoryWindowMetricsNativeRepository {

	private final EntityManager em;

	public int upsertIncremental(LocalDate asOfDate, int windowDays) {
		LocalDate prevDate = asOfDate.minusDays(1);
		LocalDate outDate = asOfDate.minusDays(windowDays);

		String sql = """ 
			INSERT INTO recipe_category_window_metrics (
				as_of_date, window_days, category,
				view_cnt, engaged_view_cnt, like_net, save_net
			)
			SELECT
				:asOfDate, :windowDays, w.category,
				GREATEST(0, w.view_cnt + COALESCE(d_in.view_cnt,0) - COALESCE(d_out.view_cnt,0)) AS view_cnt,
				GREATEST(0, w.engaged_view_cnt + COALESCE(d_in.engaged_view_cnt,0) - COALESCE(d_out.engaged_view_cnt,0)) AS engaged_view_cnt,
				(w.like_net + COALESCE(d_in.like_cnt,0) - COALESCE(d_out.like_cnt,0)) AS like_net,
				(w.save_net + COALESCE(d_in.save_cnt,0) - COALESCE(d_out.save_cnt,0)) AS save_net
			FROM recipe_category_window_metrics w
			LEFT JOIN recipe_category_daily_metrics d_in
				ON d_in.metric_date = :asOfDate AND d_in.category = w.category
			LEFT JOIN recipe_category_daily_metrics d_out
				ON d_out.metric_date = :outDate AND d_out.category = w.category
			WHERE w.as_of_date = :prevDate AND w.window_days = :windowDays
			ON DUPLICATE KEY UPDATE
				view_cnt = VALUES(view_cnt),
				engaged_view_cnt = VALUES(engaged_view_cnt),
				like_net = VALUES(like_net),
				save_net = VALUES(save_net),
				modified_at = NOW(6);
		""";

		return em.createNativeQuery(sql)
			.setParameter("asOfDate", asOfDate)
			.setParameter("prevDate", prevDate)
			.setParameter("outDate", outDate)
			.setParameter("windowDays", windowDays)
			.executeUpdate();
	}

	public int seedNewFromDaily(LocalDate asOfDate, int windowDays) {
		String sql = """ 
			INSERT INTO recipe_category_window_metrics (as_of_date, window_days, category, view_cnt, engaged_view_cnt, like_net, save_net)
			SELECT
				:asOfDate, :windowDays, d.category,
				COALESCE(d.view_cnt,0),
				COALESCE(d.engaged_view_cnt,0),
				COALESCE(d.like_cnt,0),
				COALESCE(d.save_cnt,0)
			FROM recipe_category_daily_metrics d
			LEFT JOIN recipe_category_window_metrics w
				ON w.as_of_date = :asOfDate AND w.window_days = :windowDays AND w.category = d.category
			WHERE d.metric_date = :asOfDate
			  AND w.category IS NULL
			ON DUPLICATE KEY UPDATE
				modified_at = NOW(6);
		""";
		return em.createNativeQuery(sql)
			.setParameter("asOfDate", asOfDate)
			.setParameter("windowDays", windowDays)
			.executeUpdate();
	}

	public int rebuildWindow(LocalDate asOfDate, int windowDays) {
		LocalDate start = asOfDate.minusDays(windowDays - 1);

		String sql = """
			INSERT INTO recipe_category_window_metrics (
				as_of_date, window_days, category,
				view_cnt, engaged_view_cnt, like_net, save_net
			)
			SELECT
				:asOfDate, :windowDays, m.category,
				SUM(m.view_cnt),
				SUM(m.engaged_view_cnt),
				SUM(m.like_cnt) - SUM(m.unlike_cnt),
				SUM(m.save_cnt) - SUM(m.unsave_cnt)
			FROM recipe_category_daily_metrics m
			WHERE m.metric_date BETWEEN :startDate AND :asOfDate
			GROUP BY m.category
			ON DUPLICATE KEY UPDATE
				view_cnt = VALUES(view_cnt),
				engaged_view_cnt = VALUES(engaged_view_cnt),
				like_net = VALUES(like_net),
				save_net = VALUES(save_net),
				modified_at = NOW(6)
		""";

		return em.createNativeQuery(sql)
			.setParameter("asOfDate", asOfDate)
			.setParameter("windowDays", windowDays)
			.setParameter("startDate", start)
			.executeUpdate();
	}
}
