package com.ongi.order.domain;

public record OrderItem(
	Long id,
	Long productId,
	String productName,
	Long price,
	Integer quantity
) {
}