package com.ongi.notification.domain.enums;

public enum NotificationType {
	PUSH("앱푸시"),
	ALIMTALK("카카오알림톡"),
	SMS("문자메시지"),
	EMAIL("이메일");

	private final String description;

	NotificationType(String description) {
		this.description = description;
	}

	public String getDescription() {
		return description;
	}
}