package com.ongi.api.recipe.messaging.consumer;

import com.ongi.api.common.persistence.enums.OutBoxEventTypeEnum;
import com.ongi.api.recipe.adapter.out.persistence.repository.RecipeProcessedEventRepository;
import com.ongi.api.recipe.adapter.out.persistence.repository.RecipeStatsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

@Service
@RequiredArgsConstructor
public class RecipeEventHandler {

	private final ObjectMapper objectMapper;

	private final RecipeProcessedEventRepository processedRepository;

	private final RecipeStatsRepository recipeStatsRepository;

	private final RecipeCacheVersionResolver recipeCacheVersionResolver;

	private final CacheManager cacheManager;

	@Transactional(transactionManager = "transactionManager")
	public void statusUpdateHandle(String json) throws Exception {
		RecipeLikeEvent e = objectMapper.readValue(json, RecipeLikeEvent.class);

		// 1) 멱등 체크
		if (!processedRepository.firstTime(e.eventId())) {
			return; // 이미 처리한 이벤트
		}

		// 2) 타입별 카운터 반영
		switch (e.eventType()) {
			//case "RECIPE_LIKED" -> recipeStatsRepository.incrementLikedCount(e.recipeId(), 1);
			//case "RECIPE_UNLIKED" -> recipeStatsRepository.incrementLikedCount(e.recipeId(), -1);
			//case "RECIPE_VIEWED" -> recipeStatsRepository.incrementLikedCount(e.recipeId(), -1);

			default -> {
				System.out.println(e.eventId() +  " : " + e.eventType() + " : " + e.recipeId() + " : " + e.userId());
			}
		}
	}

	@Transactional(transactionManager = "transactionManager")
	public void cacheHandle(String json) throws Exception {
		RecipeEvent e = objectMapper.readValue(json, RecipeEvent.class);

		// 1) 멱등 체크
		if (!processedRepository.firstTime(e.eventId())) {
			return; // 이미 처리한 이벤트
		}

		// 2) 타입별 카운터 반영
		switch (e.eventType()) {
			case "RECIPE_CREATED" -> evictListCache();
			case "RECIPE_UPDATED", "RECIPE_DELETED" ->  {
				recipeCacheVersionResolver.bump(e.recipeId());
				evictListCache();
			}
			default -> {
				System.out.println("Do Nothing : " + e.eventId() +  " : " + e.eventType() + " : " + e.recipeId() + " : " + e.userId());
			}
		}
	}

	private void evictListCache() {
		Cache cache = cacheManager.getCache("recipeList");
		if(cache != null) cache.clear();
	}

	// TODO Kafka DLQ Handler + ConcurrentKafkaListenerContainerFactory
}
