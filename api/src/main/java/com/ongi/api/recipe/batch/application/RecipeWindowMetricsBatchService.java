package com.ongi.api.recipe.batch.application;

import com.ongi.api.recipe.adapter.out.persistence.metrics.repository.RecipeCategoryWindowMetricsRepository;
import com.ongi.api.recipe.adapter.out.persistence.metrics.repository.RecipeWindowMetricsRepository;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class RecipeWindowMetricsBatchService {

	private final RecipeWindowMetricsRepository windowRepository;

	private final RecipeCategoryWindowMetricsRepository categoryWindowRepository;

	@Transactional(transactionManager = "transactionManager")
	public void refreshTodayWindows() {
		LocalDate asOf = LocalDate.now().minusDays(1);
		LocalDate prev = asOf.minusDays(1);


		for (int windowDays : new int[]{7, 30}) {
			LocalDate out  = asOf.minusDays(windowDays);
			LocalDate start = asOf.minusDays(windowDays - 1);

			// 증분
			windowRepository.upsertIncremental(asOf, prev, out, windowDays);
			windowRepository.seedNewFromDaily(asOf, windowDays);

			categoryWindowRepository.upsertIncremental(asOf, prev, out, windowDays);
			categoryWindowRepository.seedNewFromDaily(asOf, windowDays);

			// 보정(옵션): 최근 2일 full rebuild
			LocalDate fixFrom = asOf.minusDays(2);
			for (LocalDate d = fixFrom; !d.isAfter(asOf); d = d.plusDays(1)) {
				windowRepository.rebuildWindow(d, windowDays, start);
				categoryWindowRepository.rebuildWindow(d, windowDays, start);
			}
		}
	}
}
