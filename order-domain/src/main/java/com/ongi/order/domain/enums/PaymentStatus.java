package com.ongi.order.domain.enums;

public enum PaymentStatus {
	READY("결제준비"),
	IN_PROGRESS("결제진행중"),
	DONE("결제완료"),
	CANCELED("결제취소"),
	PARTIAL_CANCELED("부분취소"),
	ABORTED("결제실패"),
	EXPIRED("유효시간만료");

	private final String description;

	PaymentStatus(String description) {
		this.description = description;
	}

	public String getDescription() {
		return description;
	}
}