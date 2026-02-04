package com.ongi.order.domain;

import com.ongi.order.domain.enums.PaymentStatus;
import java.time.LocalDateTime;

public record Payment(
	Long id,
	Long orderId,
	String paymentKey,
	String method,
	Long amount,
	PaymentStatus status,
	LocalDateTime approvedAt
) {
}