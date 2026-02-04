package com.ongi.notification.service.adapter.in.messaging;

import com.ongi.api.order.messaging.event.OrderPaidEvent;
import com.ongi.notification.domain.NotificationRequest;
import com.ongi.notification.domain.enums.NotificationType;
import com.ongi.notification.service.application.NotificationService;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationConsumer {

	private final NotificationService notificationService;

	@KafkaListener(topics = "order.paid", groupId = "notification-service-group")
	public void handleOrderPaid(OrderPaidEvent event) {
		log.info("[Consumer] 결제 완료 이벤트 수신: orderId={}", event.orderId());
		
		NotificationRequest request =
			new NotificationRequest(
				event.userId(),
				NotificationType.ALIMTALK,
				"결제 완료",
				String.format("주문번호 [%s] 결제가 완료되었습니다.", event.orderId()),
				"ORDER_PAID:" + event.orderId(),
				null
			);

		try {
			notificationService.processNotification(request).block(Duration.ofSeconds(10));
		} catch (Exception e) {
			log.error("알림 처리 중 오류 발생", e);
			throw e; // 예외를 던져야 Kafka가 재시도 로직을 수행함
		}
	}

	/**
	 * 2. 공통 알림 토픽 구독 (General)
	 * 다른 서비스에서 직접 NotificationRequest 규격으로 메시지를 던지는 경우
	 */
	@KafkaListener(topics = "notification.request", groupId = "notification-service-group")
	public void handleGeneralNotification(NotificationRequest request) {
		log.info("[Consumer] 공통 알림 요청 수신: eventId={}", request.eventId());
		try {
			notificationService.processNotification(request).block(Duration.ofSeconds(10));
		} catch (Exception e) {
			log.error("알림 처리 중 오류 발생", e);
			throw e; // 예외를 던져야 Kafka가 재시도 로직을 수행함
		}
	}
}