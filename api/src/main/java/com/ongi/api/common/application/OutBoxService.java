package com.ongi.api.common.application;

import com.ongi.api.common.persistence.entity.OutBoxEventEntity;
import com.ongi.api.common.persistence.entity.repository.OutBoxEventRepository;
import com.ongi.api.common.persistence.enums.OutBoxAggregateTypeEnum;
import com.ongi.api.common.persistence.enums.OutBoxEventTypeEnum;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

@RequiredArgsConstructor
@Service
public class OutBoxService {

	private final OutBoxEventRepository outBoxEventRepository;

	private final ObjectMapper objectMapper;

	public void enqueuePending(UUID eventId, OutBoxAggregateTypeEnum aggregateTypeEnum, long aggregateId, OutBoxEventTypeEnum eventTypeEnum, Object payloadObject) {
		String payload;
		try {
			payload = objectMapper.writeValueAsString(payloadObject);
		} catch (Exception e) {
			throw new IllegalArgumentException("Payload could not be converted to JSON", e);
		}

		outBoxEventRepository.save(
			OutBoxEventEntity.createPending(eventId, aggregateTypeEnum, aggregateId, eventTypeEnum, payload)
		);
	}
}
