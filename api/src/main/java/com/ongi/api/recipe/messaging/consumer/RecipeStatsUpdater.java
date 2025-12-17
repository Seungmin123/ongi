package com.ongi.api.recipe.messaging.consumer;

import com.ongi.api.recipe.persistence.repository.RecipeProcessedEventRepository;
import com.ongi.api.recipe.persistence.repository.RecipeStatsRepository;
import com.ongi.api.user.messaging.consumer.UserActionEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

@Service
@RequiredArgsConstructor
public class RecipeStatsUpdater {

	private final ObjectMapper objectMapper;

	private final RecipeProcessedEventRepository processedRepository;

	private final RecipeStatsRepository recipeStatsRepository;

	@Transactional(transactionManager = "transactionManager")
	public void handle(String json) throws Exception {
		RecipeLikeEvent e = objectMapper.readValue(json, RecipeLikeEvent.class);

		// 1) 멱등 체크
		if (!processedRepository.firstTime(e.eventId())) {
			return; // 이미 처리한 이벤트
		}

		// 2) 타입별 카운터 반영
		switch (e.eventType()) {
			//case "RECIPE_LIKED" -> recipeStatsRepository.incrementLikedCount(e.recipeId(), 1);
			//case "RECIPE_UNLIKED" -> recipeStatsRepository.incrementLikedCount(e.recipeId(), -1);

			default -> {
				System.out.println(e.eventId() +  " : " + e.eventType() + " : " + e.recipeId() + " : " + e.userId());
			}
		}
	}

	// TODO Kafka DLQ Handler + ConcurrentKafkaListenerContainerFactory
}
