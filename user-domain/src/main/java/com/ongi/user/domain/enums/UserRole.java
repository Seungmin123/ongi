package com.ongi.user.domain.enums;

public enum UserRole {

	GUEST("ROLE_GUEST"),
	USER("ROLE_USER"),
	ENGINEER("ROLE_ENGINEER"),
	ADMIN("ROLE_ADMIN");

	private final String key;

	UserRole(String key) {
		this.key = key;
	}

	public String getKey() {
		return key;
	}
}
