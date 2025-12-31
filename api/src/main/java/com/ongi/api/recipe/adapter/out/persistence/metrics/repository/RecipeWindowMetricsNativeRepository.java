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
public class RecipeWindowMetricsNativeRepository {

	private final EntityManager em;

	/**
	 * asOfDate 기준 windowDays(7/30) 집계 결과를 recipe_window_metrics에 upsert.
	 *
	 * MySQL 8: INSERT INTO ... SELECT ... ON DUPLICATE KEY UPDATE 패턴
	 */
	public int upsertWindow(LocalDate asOfDate, int windowDays) {
		// windowDays=7이면 6일 전부터 포함 (총 7일)
		// windowDays=30이면 29일 전부터 포함
		String sql = """
			INSERT INTO recipe_window_metrics (
			  as_of_date, window_days, recipe_id,
			  view_cnt, engaged_view_cnt,
			  like_net, save_net,
			  dwell_ms_sum, scroll_depth_sum
			)
			SELECT
			  CURDATE() AS as_of_date,
			  ?        AS window_days,
			  recipe_id,
			  SUM(view_cnt) AS view_cnt,
			  SUM(engaged_view_cnt) AS engaged_view_cnt,
			  (SUM(like_cnt) - SUM(unlike_cnt)) AS like_net,
			  (SUM(save_cnt) - SUM(unsave_cnt)) AS save_net,
			  SUM(dwell_ms_sum) AS dwell_ms_sum,
			  SUM(scroll_depth_sum) AS scroll_depth_sum
			FROM recipe_daily_metrics
			WHERE metric_date >= CURDATE() - INTERVAL ? DAY
			GROUP BY recipe_id
			ON DUPLICATE KEY UPDATE
			  view_cnt = VALUES(view_cnt),
			  engaged_view_cnt = VALUES(engaged_view_cnt),
			  like_net = VALUES(like_net),
			  save_net = VALUES(save_net),
			  dwell_ms_sum = VALUES(dwell_ms_sum),
			  scroll_depth_sum = VALUES(scroll_depth_sum),
			  updated_at = NOW(6)
		""";

		int intervalDays = windowDays - 1;

		return em.createNativeQuery(sql)
			.setParameter(1, windowDays)
			.setParameter(2, intervalDays)
			.executeUpdate();
	}

	/**
	 * window를 새로 계산할 때, "이 window에 존재하지만 이번 계산에서 빠진 레시피"를 0으로 만들고 싶으면:
	 * - 선택지 A: delete 후 insert (가장 단순, 락/부하 주의)
	 * - 선택지 B: 별도 cleanup 쿼리
	 *
	 * 여기선 A안(안전하고 단순) 제공.
	 */
	public int deleteWindow(LocalDate asOfDate, int windowDays) {
		String sql = """
			DELETE FROM recipe_window_metrics
			WHERE as_of_date = ?
			  AND window_days = ?
		""";
		return em.createNativeQuery(sql)
			.setParameter(1, Date.valueOf(asOfDate))
			.setParameter(2, windowDays)
			.executeUpdate();
	}

}
