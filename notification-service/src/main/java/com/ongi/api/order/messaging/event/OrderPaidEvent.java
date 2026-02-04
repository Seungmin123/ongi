package com.ongi.api.order.messaging.event;

import java.time.LocalDateTime;

/**
 * api 모듈에서 발행한 OrderPaidEvent를 받기 위한 역직렬화용 클래스
 */
public record OrderPaidEvent(
	Long orderId,
	Long userId,
	Long totalPrice,
	LocalDateTime paidAt
) {
}
