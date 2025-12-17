package com.ongi.api.user.messaging.consumer;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserActionConsumer {

	private final UserStatsUpdater userStatsUpdater;

	@KafkaListener(
		topics = "user-action-events",
		groupId = "user-stats-updater"
	)
	public void onMessage(String message, Acknowledgment ack) throws Exception {
		userStatsUpdater.handle(message);
		ack.acknowledge(); // DB 처리 성공 후 커밋
	}
}