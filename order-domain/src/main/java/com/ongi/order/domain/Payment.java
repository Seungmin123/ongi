package com.ongi.order.domain;

import com.ongi.order.domain.enums.PaymentStatus;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
public class Payment {
	private final Long id;
	private final Long orderId;
	private final String paymentKey;
	private final String method;
	private final Long amount;
	private final PaymentStatus status;
	private final LocalDateTime approvedAt;

	@Builder
	public Payment(Long id, Long orderId, String paymentKey, String method, Long amount, PaymentStatus status, LocalDateTime approvedAt) {
		this.id = id;
		this.orderId = orderId;
		this.paymentKey = paymentKey;
		this.method = method;
		this.amount = amount;
		this.status = status;
		this.approvedAt = approvedAt;
	}
}
