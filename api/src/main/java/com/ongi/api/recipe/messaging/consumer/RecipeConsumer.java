package com.ongi.api.recipe.messaging.consumer;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RecipeConsumer {

	private final RecipeEventHandler recipeEventHandler;

	private final RecipeMetricsEventHandler recipeMetricsEventHandler;

//	@KafkaListener(
//		topics = "recipe-like-events",
//		groupId = "recipe-stats-updater"
//	)
	public void onLikeMessage(String message, Acknowledgment ack) throws Exception {
		recipeEventHandler.statusUpdateHandle(message);
		ack.acknowledge(); // DB 처리 성공 후 커밋
	}

	/**
	 * 캐시 무효화
	 */
	@KafkaListener(
		topicPattern = "recipe\\.(created|updated|deleted)",
		groupId = "recipe-cache-invalidator"
	)
	public void onMessage(String message, Acknowledgment ack) throws Exception {
		recipeEventHandler.cacheHandle(message);
		ack.acknowledge();
	}

	/**
	 *  메트릭 집계: view/like/save/comment 등 행동 이벤트
	 */
	@KafkaListener(
		topics = "recipe.events",
		groupId = "recipe-metrics-upserter"
	)
	public void onRecipeEventForMetrics(String message, Acknowledgment ack) throws Exception {
		recipeMetricsEventHandler.handle(message);
		ack.acknowledge();
	}
}