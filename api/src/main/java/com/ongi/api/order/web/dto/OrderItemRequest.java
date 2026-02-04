package com.ongi.api.order.web.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record OrderItemRequest(
	@NotNull
	Long productId,
	@NotNull
	String productName,
	@NotNull @Min(0)
	Long price,
	@NotNull @Min(1)
	Integer quantity
) {}
