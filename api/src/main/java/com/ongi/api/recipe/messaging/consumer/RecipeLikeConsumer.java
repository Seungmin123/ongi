package com.ongi.api.recipe.messaging.consumer;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RecipeLikeConsumer {

	private final RecipeStatsUpdater recipeStatsUpdater;

//	@KafkaListener(
//		topics = "recipe-like-events",
//		groupId = "recipe-stats-updater"
//	)
	public void onLikeMessage(String message, Acknowledgment ack) throws Exception {
		recipeStatsUpdater.handle(message);
		ack.acknowledge(); // DB 처리 성공 후 커밋
	}

//	@KafkaListener(
//		topics = "recipe-view-events",
//		groupId = "recipe-stats-updater"
//	)
	public void onViewMessage(String message, Acknowledgment ack) throws Exception {
		recipeStatsUpdater.handle(message);
		ack.acknowledge(); // DB 처리 성공 후 커밋
	}
}