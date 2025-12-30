package com.ongi.api.ingredients.batch.scheduler;

import com.ongi.api.ingredients.batch.application.IngredientIdfBatchService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class IngredientIdfScheduler {

	private final IngredientIdfBatchService batchService;

	// 매일 새벽 4시
	@Scheduled(cron = "0 0 4 * * *")
	public void run() {
		batchService.recomputeIdf();
	}
}
