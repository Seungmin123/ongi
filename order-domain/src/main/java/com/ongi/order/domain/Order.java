package com.ongi.order.domain;

import com.ongi.order.domain.enums.OrderStatus;
import java.time.LocalDateTime;
import java.util.List;

public record Order(
	Long id,
	Long userId,
	String orderNumber,
	Long totalPrice,
	OrderStatus status,
	LocalDateTime orderedAt,
	List<OrderItem> orderItems
) {
}