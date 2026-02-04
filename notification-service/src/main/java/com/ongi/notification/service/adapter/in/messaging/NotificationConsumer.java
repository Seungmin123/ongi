package com.ongi.notification.service.adapter.in.messaging;

import com.ongi.api.order.messaging.event.OrderPaidEvent;
import com.ongi.notification.domain.NotificationRequest;
import com.ongi.notification.domain.enums.NotificationType;
import com.ongi.notification.service.application.NotificationService;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.kafka.receiver.KafkaReceiver;
import reactor.kafka.receiver.ReceiverRecord;
import reactor.util.retry.Retry;
import tools.jackson.databind.ObjectMapper;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationConsumer {

	private final KafkaReceiver<String, String> kafkaReceiver;
	private final NotificationService notificationService;
	private final ObjectMapper objectMapper;

	@EventListener(ApplicationReadyEvent.class)
	public void startConsuming() {
		kafkaReceiver.receive()
			.flatMap(record -> {
				log.debug("[Consumer] 메시지 수신 - Topic: {}, Offset: {}", record.topic(), record.offset());
				
				return processRecord(record)
					.retryWhen(Retry.backoff(3, Duration.ofMillis(100)) // 일시적 오류 3회 재시도
						.doAfterRetry(signal -> log.warn("메시지 처리 재시도 중... {}", signal.failure().getMessage())))
					.onErrorResume(e -> {
						// 재시도 실패 시 로그 남기고 건너뜀 (스트림 중단 방지)
						log.error("메시지 처리 최종 실패 (Skip): {}", record.value(), e);
						return Mono.empty();
					})
					.then(record.receiverOffset().commit()); // 처리가 성공하거나 에러 처리된 후 오프셋 커밋
			})
			.subscribe(
				null,
				e -> log.error("Kafka Consumer 스트림 비정상 종료 (치명적 오류)", e) // 이 로그가 보이면 앱 재시작 필요
			);
	}

	private Mono<Void> processRecord(ReceiverRecord<String, String> record) {
		try {
			String topic = record.topic();
			String json = record.value();

			if ("order.paid".equals(topic)) {
				OrderPaidEvent event = objectMapper.readValue(json, OrderPaidEvent.class);
				return handleOrderPaid(event);
			} else if ("notification.request".equals(topic)) {
				NotificationRequest request = objectMapper.readValue(json, NotificationRequest.class);
				return notificationService.processNotification(request);
			}
			return Mono.empty();
		} catch (Exception e) {
			return Mono.error(e); // JSON 파싱 에러 등을 리턴하여 재시도/에러처리 흐름으로 넘김
		}
	}

	private Mono<Void> handleOrderPaid(OrderPaidEvent event) {
		log.info("[Consumer] 결제 완료 이벤트 수신: orderId={}", event.orderId());

		NotificationRequest request = new NotificationRequest(
			event.userId(),
			NotificationType.ALIMTALK,
			"결제 완료",
			String.format("주문번호 [%s] 결제가 완료되었습니다.", event.orderId()),
			"ORDER_PAID:" + event.orderId(),
			null
		);

		return notificationService.processNotification(request);
	}
}
