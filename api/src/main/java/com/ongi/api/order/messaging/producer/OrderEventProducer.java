package com.ongi.api.order.messaging.producer;

import com.ongi.api.order.messaging.event.OrderPaidEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventProducer {

	private final KafkaTemplate<String, Object> kafkaTemplate;
	private static final String TOPIC = "order.paid";

	public void sendPaidEvent(OrderPaidEvent event) {
		kafkaTemplate.send(TOPIC, String.valueOf(event.orderId()), event)
			.whenComplete((result, ex) -> {
				if (ex == null) {
					log.info("주문 결제 이벤트 발행 성공: {}", event.orderId());
				} else {
					log.error("주문 결제 이벤트 발행 실패: {}", event.orderId(), ex);
				}
			});
	}
}
