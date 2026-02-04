package com.ongi.order.domain;

import com.ongi.order.domain.enums.OrderStatus;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
public class Order {
	private final Long id;
	private final Long userId;
	private final String orderNumber;
	private final Long totalPrice;
	private final OrderStatus status;
	private final LocalDateTime orderedAt;
	private final List<OrderItem> orderItems;

	@Builder
	public Order(Long id, Long userId, String orderNumber, Long totalPrice, OrderStatus status, LocalDateTime orderedAt, List<OrderItem> orderItems) {
		this.id = id;
		this.userId = userId;
		this.orderNumber = orderNumber;
		this.totalPrice = totalPrice;
		this.status = status;
		this.orderedAt = orderedAt;
		this.orderItems = orderItems;
	}
}
