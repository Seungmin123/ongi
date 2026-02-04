package com.ongi.order.domain.enums;

public enum DeliveryStatus {
	READY("배송준비"),
	IN_PROGRESS("배송중"),
	COMPLETED("배송완료"),
	CANCELED("배송취소");

	private final String description;

	DeliveryStatus(String description) {
		this.description = description;
	}

	public String getDescription() {
		return description;
	}
}