package com.ongi.api.recipe.messaging.consumer;

import com.ongi.api.common.persistence.enums.OutBoxEventTypeEnum;
import com.ongi.api.recipe.adapter.out.cache.RecipeCategoryCacheStore;
import com.ongi.api.recipe.adapter.out.persistence.metrics.repository.RecipeCategoryDailyMetricsRepository;
import com.ongi.api.recipe.adapter.out.persistence.metrics.repository.RecipeDailyMetricsRepository;
import com.ongi.api.recipe.adapter.out.persistence.repository.RecipeProcessedEventRepository;
import com.ongi.api.recipe.adapter.out.persistence.repository.RecipeRepository;
import java.time.LocalDate;
import java.util.function.Consumer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

@Service
@RequiredArgsConstructor
public class RecipeMetricsEventHandler {

	private final ObjectMapper objectMapper;

	private final RecipeProcessedEventRepository processedRepository;

	private final RecipeDailyMetricsRepository recipeDailyMetricsRepository;

	private final RecipeCategoryDailyMetricsRepository categoryDailyRepository;

	private final RecipeCategoryCacheStore recipeCategoryCacheStore;

	private final RecipeRepository recipeRepository;

	@Transactional(transactionManager = "transactionManager")
	public void handle(String json) throws Exception {
		RecipeEvent e = objectMapper.readValue(json, RecipeEvent.class);

		// 멱등 체크
		if (!processedRepository.firstTime(e.eventId())) {
			return; // 이미 처리한 이벤트
		}

		long recipeId = e.recipeId();
		LocalDate metricDate = e.occurredAt().toLocalDate();
		String category = resolveCategory(recipeId, e.category());
		OutBoxEventTypeEnum type = OutBoxEventTypeEnum.valueOf(e.eventType());

		switch (type) {
			case RECIPE_VIEW -> {
				recipeDailyMetricsRepository.upsertView(metricDate, recipeId);
				withCategory(category, c -> categoryDailyRepository.upsertView(metricDate, c));
			}

			case RECIPE_LIKED -> {
				recipeDailyMetricsRepository.upsertLike(metricDate, recipeId);
				if (category != null) categoryDailyRepository.upsertLike(metricDate, category);
				withCategory(category, c -> categoryDailyRepository.upsertView(metricDate, c));
			}
			case RECIPE_UNLIKED -> {
				recipeDailyMetricsRepository.upsertUnlike(metricDate, recipeId);
				withCategory(category, c -> categoryDailyRepository.upsertUnlike(metricDate, c));
			}

			case RECIPE_BOOKMARKED -> {
				recipeDailyMetricsRepository.upsertSave(metricDate, recipeId);
				withCategory(category, c -> categoryDailyRepository.upsertSave(metricDate, c));
			}
			case RECIPE_UNBOOKMARKED -> {
				recipeDailyMetricsRepository.upsertUnsave(metricDate, recipeId);
				withCategory(category, c -> categoryDailyRepository.upsertUnsave(metricDate, c));
			}

			// engaged view(스크롤/체류 등) 이벤트 타입을 따로 만들었다면 여기서 처리
			// case RECIPE_ENGAGED_VIEW -> { ... }

			default -> {
				// metrics 관심 없는 이벤트는 무시
			}
		}
	}

	private String resolveCategory(long recipeId, String categoryFromEvent) {
		if (categoryFromEvent != null && !categoryFromEvent.isBlank()) {
			return categoryFromEvent.trim();
		}
		return recipeCategoryCacheStore.getOrLoad(
			recipeId,
			() -> recipeRepository.findCategoryById(recipeId)
		);
	}

	private void withCategory(String category, Consumer<String> fn) {
		if (category != null && !category.isBlank()) fn.accept(category.trim());
	}
}
