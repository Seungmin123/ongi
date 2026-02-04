package com.ongi.order.domain;

import com.ongi.order.domain.enums.DeliveryStatus;
import lombok.Builder;
import lombok.Getter;

@Getter
public class Delivery {
	private final Long id;
	private final Long orderId;
	private final String trackingNumber;
	private final String courier;
	private final DeliveryStatus status;
	private final String address;
	private final String receiverName;
	private final String receiverPhone;

	@Builder
	public Delivery(Long id, Long orderId, String trackingNumber, String courier, DeliveryStatus status, String address, String receiverName, String receiverPhone) {
		this.id = id;
		this.orderId = orderId;
		this.trackingNumber = trackingNumber;
		this.courier = courier;
		this.status = status;
		this.address = address;
		this.receiverName = receiverName;
		this.receiverPhone = receiverPhone;
	}
}
