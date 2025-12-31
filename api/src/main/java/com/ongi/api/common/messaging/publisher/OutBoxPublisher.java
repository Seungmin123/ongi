package com.ongi.api.common.messaging.publisher;

import com.ongi.api.common.persistence.entity.OutBoxEventEntity;
import com.ongi.api.common.persistence.entity.repository.OutBoxEventRepository;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
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
				publishFanoutAsync(e);        // Kafka 발행
				outBoxPublisherTx.markSuccess(e.getId());  // DB 업데이트
			} catch (Exception ex) {
				outBoxPublisherTx.handleFailure(e, ex);
			}
		}
	}

	private void publishFanoutAsync(OutBoxEventEntity e) throws Exception {
		JsonNode payload = objectMapper.readTree(e.getPayload());
		PublishPlan plan = OutboxPublishRouter.plan(e.getEventType(), payload);
		if (plan.isEmpty()) return;

		String value = e.getPayload();
		List<PublishTarget> targets = plan.targets();

		AtomicInteger remaining = new AtomicInteger(targets.size());
		Throwable[] firstError = new Throwable[1];

		for (PublishTarget target : targets) {
			CompletableFuture<SendResult<String, String>> future =
				kafkaTemplate.send(target.topic(), target.key(), value);

			future.whenComplete((result, ex) -> {
				if (ex != null && firstError[0] == null) {
					firstError[0] = ex;
				}

				if (remaining.decrementAndGet() == 0) {
					if (firstError[0] == null) {
						outBoxPublisherTx.markSuccess(e.getId());
					} else {
						outBoxPublisherTx.handleFailure(e, firstError[0]);
					}
				}
			});
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
