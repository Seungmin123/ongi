package com.ongi.product.domain;

public record Product(
	Long id,
	String name,
	Long price,
	Integer stock,
	Long ingredientId
) {
}