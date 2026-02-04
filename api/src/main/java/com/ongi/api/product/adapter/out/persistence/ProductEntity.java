package com.ongi.api.product.adapter.out.persistence;

import com.ongi.api.common.persistence.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "product")
public class ProductEntity extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String name;

	@Column(nullable = false)
	private Long price;

	@Column(nullable = false)
	private Integer stock;

	private Long ingredientId;

	@Builder
	public ProductEntity(Long id, String name, Long price, Integer stock, Long ingredientId) {
		this.id = id;
		this.name = name;
		this.price = price;
		this.stock = stock;
		this.ingredientId = ingredientId;
	}

	public void decreaseStock(int quantity) {
		if (this.stock < quantity) {
			throw new IllegalStateException("재고가 부족합니다: " + this.name);
		}
		this.stock -= quantity;
	}

	public void increaseStock(int quantity) {
		this.stock += quantity;
	}
}
