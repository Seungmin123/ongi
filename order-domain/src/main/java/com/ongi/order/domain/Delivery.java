package com.ongi.order.domain;

import com.ongi.order.domain.enums.DeliveryStatus;

public record Delivery(
	Long id,
	Long orderId,
	String trackingNumber,
	String courier,
	DeliveryStatus status,
	String address,
	String receiverName,
	String receiverPhone
) {
}