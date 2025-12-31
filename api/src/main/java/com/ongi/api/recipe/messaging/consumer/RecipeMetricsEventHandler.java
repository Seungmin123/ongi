package com.ongi.api.recipe.messaging.consumer;

import com.ongi.api.common.persistence.enums.OutBoxEventTypeEnum;
import com.ongi.api.recipe.adapter.out.persistence.metrics.repository.RecipeCategoryDailyMetricsNativeRepository;
import com.ongi.api.recipe.adapter.out.persistence.metrics.repository.RecipeDailyMetricsNativeRepository;
import com.ongi.api.recipe.adapter.out.persistence.repository.RecipeProcessedEventRepository;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

@Service
@RequiredArgsConstructor
public class RecipeMetricsEventHandler {

	private final ObjectMapper objectMapper;

	private final RecipeProcessedEventRepository processedRepository;

	private final RecipeDailyMetricsNativeRepository recipeDailyMetricsRepo;

	private final RecipeCategoryDailyMetricsNativeRepository recipeCategoryDailyRepo;

	public void handle(String json) throws Exception {
		RecipeEvent e = objectMapper.readValue(json, RecipeEvent.class);

		long recipeId = e.recipeId();
		LocalDate metricDate = e.occurredAt().toLocalDate();
		String category = e.category();
		OutBoxEventTypeEnum type = OutBoxEventTypeEnum.valueOf(e.eventType());

		// 멱등 체크
		if (!processedRepository.firstTime(e.eventId())) {
			return; // 이미 처리한 이벤트
		}

		switch (type) {
			case RECIPE_VIEW -> {
				recipeDailyMetricsRepo.upsertView(metricDate, recipeId);
				if (category != null) recipeCategoryDailyRepo.upsertView(metricDate, category);
			}

			case RECIPE_LIKED -> {
				recipeDailyMetricsRepo.upsertLike(metricDate, recipeId);
				if (category != null) recipeCategoryDailyRepo.upsertLike(metricDate, category);
			}
			case RECIPE_UNLIKED -> {
				recipeDailyMetricsRepo.upsertUnlike(metricDate, recipeId);
				if (category != null) recipeCategoryDailyRepo.upsertUnlike(metricDate, category);
			}

			case RECIPE_BOOKMARKED -> {
				recipeDailyMetricsRepo.upsertSave(metricDate, recipeId);
				if (category != null) recipeCategoryDailyRepo.upsertSave(metricDate, category);
			}
			case RECIPE_UNBOOKMARKED -> {
				recipeDailyMetricsRepo.upsertUnsave(metricDate, recipeId);
				if (category != null) recipeCategoryDailyRepo.upsertUnsave(metricDate, category);
			}

			// engaged view(스크롤/체류 등) 이벤트 타입을 따로 만들었다면 여기서 처리
			// case RECIPE_ENGAGED_VIEW -> { ... }

			default -> {
				// metrics 관심 없는 이벤트는 무시
			}
		}
	}
}
