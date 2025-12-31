package com.ongi.api.recipe.batch.scheduler;

import com.ongi.api.recipe.batch.application.RecipeWindowMetricsBatchService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class RecipeWindowMetricsScheduler {

	private final RecipeWindowMetricsBatchService batchService;

	/**
	 * 매일 00:10 실행
	 */
	@Scheduled(cron = "0 10 0 * * *")
	public void refresh() {
		batchService.refreshTodayWindows();
	}
}
