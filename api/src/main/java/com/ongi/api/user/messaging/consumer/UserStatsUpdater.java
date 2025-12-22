package com.ongi.api.user.messaging.consumer;

import com.ongi.api.user.adapter.out.cache.persistence.repository.UserProcessedEventRepository;
import com.ongi.api.user.adapter.out.cache.persistence.repository.UserStatsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

@Service
@RequiredArgsConstructor
public class UserStatsUpdater {

	private final ObjectMapper objectMapper;
	private final UserProcessedEventRepository processedRepo;
	private final UserStatsRepository userStatsRepo;

	@Transactional(transactionManager = "transactionManager")
	public void handle(String json) throws Exception {
		UserActionEvent e = objectMapper.readValue(json, UserActionEvent.class);

		// 1) 멱등 체크
		if (!processedRepo.firstTime(e.eventId())) {
			return; // 이미 처리한 이벤트
		}

		// 2) 타입별 카운터 반영
		switch (e.eventType()) {
			/*case "RECIPE_LIKED" -> userStatsRepo.incrementLikedCount(e.userId(), +1);
			case "RECIPE_UNLIKED" -> userStatsRepo.incrementLikedCount(e.userId(), -1);

			case "BOOKMARK_CREATED" -> userStatsRepo.incrementSavedCount(e.userId(), +1);
			case "BOOKMARK_DELETED" -> userStatsRepo.incrementSavedCount(e.userId(), -1);*/

			default -> {
				System.out.println("------------------------------------------------------------------------------------");
				System.out.println("------------------------------------------------------------------------------------");
				System.out.println("------------------------------------------------------------------------------------");
				System.out.println("------------------------------------------------------------------------------------");
				System.out.println("------------------------------------------------------------------------------------");
				System.out.println(e.eventId() +  " : " + e.eventType() + " : " + e.recipeId() + " : " + e.userId());
				System.out.println("------------------------------------------------------------------------------------");
				System.out.println("------------------------------------------------------------------------------------");
				System.out.println("------------------------------------------------------------------------------------");
				System.out.println("------------------------------------------------------------------------------------");
				System.out.println("------------------------------------------------------------------------------------");

			}
		}
	}
}
