package com.ongi.api.recipe.batch.application;

import com.ongi.api.recipe.adapter.out.persistence.metrics.repository.RecipeCategoryWindowMetricsNativeRepository;
import com.ongi.api.recipe.adapter.out.persistence.metrics.repository.RecipeWindowMetricsNativeRepository;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class RecipeWindowMetricsBatchService {

	private final RecipeWindowMetricsNativeRepository windowRepository;

	private final RecipeCategoryWindowMetricsNativeRepository categoryWindowRepository;

	@Transactional(transactionManager = "transactionManager")
	public void refreshTodayWindows() {
		LocalDate asOf = LocalDate.now().minusDays(1);

		for (int w : new int[]{7, 30}) {
			// 증분
			windowRepository.upsertIncremental(asOf, w);
			windowRepository.seedNewFromDaily(asOf, w);

			categoryWindowRepository.upsertIncremental(asOf, w);
			categoryWindowRepository.seedNewFromDaily(asOf, w);

			// 보정(옵션): 최근 2일 full rebuild
			LocalDate fixFrom = asOf.minusDays(2);
			for (LocalDate d = fixFrom; !d.isAfter(asOf); d = d.plusDays(1)) {
				windowRepository.rebuildWindow(d, w);
				categoryWindowRepository.rebuildWindow(d, w);
			}
		}
	}
}
