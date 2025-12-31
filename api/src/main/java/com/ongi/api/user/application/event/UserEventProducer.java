package com.ongi.api.user.application.event;

import com.ongi.api.user.web.dto.UserEventBatchRequest;
import com.ongi.api.user.web.dto.UserEventDto;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@RequiredArgsConstructor
@Component
public class UserEventProducer {

	private final KafkaTemplate<String, String> kafkaTemplate;

	private final ObjectMapper objectMapper;

	public void publish(Long userId, UserEventBatchRequest batch, UserEventDto e) {

		UserEventMessage userEvent = new UserEventMessage(
			userId,
			batch.sessionId(),
			batch.deviceId(),
			batch.clientTs(),

			e.eventId(),
			e.type(),
			e.occurredAt(),

			e.pageKey(),
			e.recipeId(),
			e.referrer(),

			Instant.now(),
			e.props()
		);
		String payload = objectMapper.writeValueAsString(userEvent);
		kafkaTemplate.send(UserEventTopics.USER_EVENTS, String.valueOf(userId), payload);
	}
}
