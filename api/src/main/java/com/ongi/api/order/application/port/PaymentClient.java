package com.ongi.api.order.application.port;

import com.ongi.api.order.adapter.out.payment.toss.TossPaymentConfirmResponse;

public interface PaymentClient {
	/**
	 * 결제 최종 승인 요청
	 */
	TossPaymentConfirmResponse confirm(String paymentKey, String orderId, Long amount);
}
