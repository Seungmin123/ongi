package com.ongi.notification.service.adapter.in.messaging;

import com.ongi.api.order.messaging.event.OrderPaidEvent;
import com.ongi.notification.domain.NotificationRequest;
import com.ongi.notification.domain.enums.NotificationType;
import com.ongi.notification.service.application.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationConsumer {

	private final NotificationService notificationService;

	/**
	 * 1. 개별 도메인 이벤트 직접 구독 (Order)
	 */
	@KafkaListener(topics = "order.paid", groupId = "notification-service-group")
	public void handleOrderPaid(OrderPaidEvent event) {
		log.info("[Consumer] 결제 완료 이벤트 수신: orderId={}", event.orderId());
		
		NotificationRequest request = NotificationRequest.builder()
			.userId(event.userId())
			.type(NotificationType.ALIMTALK)
			.title("결제 완료")
			.content(String.format("주문번호 [%s] 결제가 완료되었습니다.", event.orderId()))
			.eventId("ORDER_PAID:" + event.orderId())
			.build();

		notificationService.processNotification(request).subscribe();
	}

	/**
	 * 2. 공통 알림 토픽 구독 (General)
	 * 다른 서비스에서 직접 NotificationRequest 규격으로 메시지를 던지는 경우
	 */
	@KafkaListener(topics = "notification.request", groupId = "notification-service-group")
	public void handleGeneralNotification(NotificationRequest request) {
		log.info("[Consumer] 공통 알림 요청 수신: eventId={}", request.eventId());
		notificationService.processNotification(request).subscribe();
	}
}