package com.ongi.order.domain.enums;

import lombok.Getter;

@Getter
public enum OrderStatus {
	PENDING("주문대기"),
	PAYMENT_WAITING("결제대기"),
	PAID("결제완료"),
	PREPARING("준비중"),
	SHIPPING("배송중"),
	COMPLETED("배송완료"),
	CANCELLED("주문취소"),
	REFUNDED("환불완료");

	private final String description;

	OrderStatus(String description) {
		this.description = description;
	}
}
