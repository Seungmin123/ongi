package com.ongi.api.community.messaging.consumer;

import com.ongi.api.community.adatper.out.persistence.repository.CommunityProcessedEventRepository;
import com.ongi.api.recipe.adapter.out.persistence.repository.RecipeProcessedEventRepository;
import com.ongi.api.recipe.adapter.out.persistence.repository.RecipeStatsRepository;
import com.ongi.api.recipe.messaging.consumer.RecipeCacheVersionResolver;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

@Service
@RequiredArgsConstructor
public class CommunityEventHandler {

	private final ObjectMapper objectMapper;

	private final CommunityProcessedEventRepository processedRepository;

	@Transactional(transactionManager = "transactionManager")
	public void postStatusHandle(String json) throws Exception {
		CommunityEvent e = objectMapper.readValue(json, CommunityEvent.class);

		// 1) 멱등 체크
		if (!processedRepository.firstTime(e.eventId())) {
			return; // 이미 처리한 이벤트
		}

		// 2) 타입별 카운터 반영
		switch (e.eventType()) {
			default -> {
				System.out.println("Do Notiong" + e.eventId() +  " : " + e.eventType() + " : " + e.recipeId() + " : " + e.userId());
			}
		}
	}

	@Transactional(transactionManager = "transactionManager")
	public void commentStatusHandle(String json) throws Exception {
		CommunityEvent e = objectMapper.readValue(json, CommunityEvent.class);

		// 1) 멱등 체크
		if (!processedRepository.firstTime(e.eventId())) {
			return; // 이미 처리한 이벤트
		}

		// 2) 타입별 카운터 반영
		switch (e.eventType()) {
			default -> {
				System.out.println("Do Nothing : " + e.eventId() +  " : " + e.eventType() + " : " + e.recipeId() + " : " + e.userId());
			}
		}
	}
}
