package com.ongi.order.domain;

import lombok.Builder;
import lombok.Getter;

@Getter
public class OrderItem {
	private final Long id;
	private final Long productId;
	private final String productName;
	private final Long price;
	private final Integer quantity;

	@Builder
	public OrderItem(Long id, Long productId, String productName, Long price, Integer quantity) {
		this.id = id;
		this.productId = productId;
		this.productName = productName;
		this.price = price;
		this.quantity = quantity;
	}
}
