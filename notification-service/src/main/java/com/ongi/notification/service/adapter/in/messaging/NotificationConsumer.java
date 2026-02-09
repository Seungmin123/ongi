package com.ongi.notification.service.adapter.in.messaging;

import tools.jackson.databind.ObjectMapper;
import com.ongi.api.order.messaging.event.OrderPaidEvent;
import com.ongi.notification.domain.NotificationRequest;
import com.ongi.notification.domain.enums.NotificationType;
import com.ongi.notification.service.application.NotificationService;
import java.time.Duration;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.kafka.receiver.KafkaReceiver;
import reactor.kafka.receiver.ReceiverRecord;
import reactor.util.retry.Retry;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationConsumer {

	private final KafkaReceiver<String, String> realTimeKafkaReceiver;
	private final NotificationService notificationService;
	private final ObjectMapper objectMapper;

	/**
	 * 앱 시작 후 Kafka 소비 시작 (Reactive Pull with Micro-batching)
	 */
	@EventListener(ApplicationReadyEvent.class)
	public void startConsuming() {
		realTimeKafkaReceiver.receive()
			.bufferTimeout(50, Duration.ofSeconds(1)) // 50개 모이거나 1초 지날 때까지 버퍼링
			.flatMap(records -> {
				if (records.isEmpty()) {
					return Mono.empty();
				}
				
				log.debug("[Batch] {}건의 메시지 배치 처리 시작", records.size());

				// 1. 레코드들을 NotificationRequest로 변환 (실패한 건 로그 찍고 제외)
				List<NotificationRequest> requests = records.stream()
					.map(this::convertToRequest)
					.filter(java.util.Objects::nonNull)
					.toList();

				// 2. 서비스 Bulk 메서드 호출 (Batch Insert -> Send -> Batch Update)
				return notificationService.processNotifications(requests)
					.retryWhen(Retry.backoff(3, Duration.ofMillis(100))
						.doAfterRetry(signal -> log.warn("배치 처리 재시도 중... {}", signal.failure().getMessage())))
					.onErrorResume(e -> {
						log.error("배치 처리 최종 실패 (Skip): {}", e.getMessage());
						return Mono.empty();
					})
					.then(Mono.fromRunnable(() -> {
						// 3. 배치 처리가 모두 끝나면 마지막 오프셋만 커밋
						records.get(records.size() - 1).receiverOffset().commit().subscribe();
					}));
			})
			.subscribe(
				null,
				e -> log.error("Kafka Consumer 스트림 비정상 종료 (치명적 오류)", e)
			);
	}

	private NotificationRequest convertToRequest(ReceiverRecord<String, String> record) {
		try {
			String topic = record.topic();
			String json = record.value();

			if ("order.paid".equals(topic)) {
				OrderPaidEvent event = objectMapper.readValue(json, OrderPaidEvent.class);
				return new NotificationRequest(
					event.userId(),
					NotificationType.ALIMTALK,
					"결제 완료",
					String.format("주문번호 [%s] 결제가 완료되었습니다.", event.orderId()),
					"ORDER_PAID:" + event.orderId(),
					null
				);
			} else if ("notification.request".equals(topic)) {
				return objectMapper.readValue(json, NotificationRequest.class);
			}
			return null;
		} catch (Exception e) {
			log.error("메시지 파싱 실패 (Skip): {}", record.value(), e);
			return null;
		}
	}
	
	// processRecord, handleOrderPaid 제거됨
}