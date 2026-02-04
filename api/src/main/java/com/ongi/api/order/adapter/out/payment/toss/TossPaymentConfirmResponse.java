package com.ongi.api.order.adapter.out.payment.toss;

import java.time.OffsetDateTime;

public record TossPaymentConfirmResponse(
	String mId,
	String lastTransactionKey,
	String paymentKey,
	String orderId,
	String orderName,
	long totalAmount,
	String status, // DONE, CANCELED, etc.
	OffsetDateTime requestedAt,
	OffsetDateTime approvedAt,
	String method // 카드, 가상계좌, 등
) {
}
