package com.ongi.api.recipe.batch.application;

import com.ongi.api.recipe.adapter.out.persistence.metrics.repository.RecipeWindowMetricsNativeRepository;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class RecipeWindowMetricsBatchService {

	private final RecipeWindowMetricsNativeRepository recipeWindowMetricsNativeRepository;

	@Transactional(transactionManager = "transactionManager")
	public void refreshTodayWindows() {
		LocalDate asOf = LocalDate.now();

		// 7d
		recipeWindowMetricsNativeRepository.deleteWindow(asOf, 7);
		recipeWindowMetricsNativeRepository.upsertWindow(asOf, 7);

		// 30d
		recipeWindowMetricsNativeRepository.deleteWindow(asOf, 30);
		recipeWindowMetricsNativeRepository.upsertWindow(asOf, 30);
	}
}
