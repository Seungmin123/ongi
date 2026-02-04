package com.ongi.api.order.web.dto;

import jakarta.validation.constraints.NotBlank;

public record PaymentConfirmRequest(
	@NotBlank
	String orderNumber,
	@NotBlank
	String paymentKey
) {
}
