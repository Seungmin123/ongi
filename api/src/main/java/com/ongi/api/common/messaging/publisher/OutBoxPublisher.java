package com.ongi.api.common.messaging.publisher;

import com.ongi.api.common.persistence.entity.OutBoxEventEntity;
import com.ongi.api.common.persistence.entity.repository.OutBoxEventRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Service
@RequiredArgsConstructor
public class OutBoxPublisher {

	private final OutBoxPublisherTx outBoxPublisherTx;

	private final OutBoxEventRepository outBoxEventRepository;

	private final KafkaTemplate<String, String> kafkaTemplate;

	private final ObjectMapper objectMapper;

	private static final int BATCH_SIZE = 300;
	private static final int STALE_SECONDS = 120;

	@Scheduled(fixedDelayString = "${spring.outbox.publisher.fixed-delay-ms:30000}")
	public void tick() {
		// 죽은 워커 회수
		outBoxPublisherTx.reclaimStale(STALE_SECONDS);

		List<Long> ids = outBoxPublisherTx.claim(BATCH_SIZE);
		if (ids.isEmpty()) return;

		List<OutBoxEventEntity> events = outBoxEventRepository.findByIdIn(ids);

		for (OutBoxEventEntity e : events) {
			try {
				publishFanout(e);        // Kafka 발행
				outBoxPublisherTx.markSuccess(e.getId());  // DB 업데이트
			} catch (Exception ex) {
				outBoxPublisherTx.handleFailure(e, ex);
			}
		}
	}

	private void publishFanout(OutBoxEventEntity e) throws Exception {
		JsonNode payload = objectMapper.readTree(e.getPayload());

		PublishPlan plan = OutboxPublishRouter.plan(e.getEventType(), payload);

		if (plan.isEmpty()) {
			return;
		}

		String value = e.getPayload();

		for (PublishTarget target : plan.targets()) {
			kafkaTemplate
				.send(target.topic(), target.key(), value)
				.get();
		}
	}
}
