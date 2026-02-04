package com.ongi.product.domain;

import lombok.Builder;
import lombok.Getter;

@Getter
public class Product {
	private final Long id;
	private final String name;
	private final Long price;
	private final Integer stock;
	private final Long ingredientId; // 연관된 식재료 ID (Optional)

	@Builder
	public Product(Long id, String name, Long price, Integer stock, Long ingredientId) {
		this.id = id;
		this.name = name;
		this.price = price;
		this.stock = stock;
		this.ingredientId = ingredientId;
	}
}
