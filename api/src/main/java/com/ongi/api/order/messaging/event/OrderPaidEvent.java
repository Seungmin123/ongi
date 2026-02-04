package com.ongi.api.order.messaging.event;

import java.time.LocalDateTime;

public record OrderPaidEvent(
	Long orderId,
	Long userId,
	Long totalPrice,
	LocalDateTime paidAt
) {
}
