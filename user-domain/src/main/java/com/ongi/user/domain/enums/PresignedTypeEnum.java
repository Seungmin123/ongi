package com.ongi.user.domain.enums;

import java.util.Arrays;

public enum PresignedTypeEnum {

	RECIPE("/recipe"),
	RECIPE_STEPS("/recipe/steps"),
	USER_PROFILE("/user/profile"),;

	private final String path;

	PresignedTypeEnum(String path) {
		this.path = path;
	}

	public String getCode() {
		return path;
	}

	public static PresignedTypeEnum from(String name) {
		if(name == null || name.isBlank()) {
			return null;
		}

		String s = name.trim().toLowerCase();

		return Arrays.stream(values())
			.filter(v -> v.path.equalsIgnoreCase(s))
			.findFirst()
			.orElseGet(() -> null);
	}
}
