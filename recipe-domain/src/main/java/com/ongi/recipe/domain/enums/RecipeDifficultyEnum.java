package com.ongi.recipe.domain.enums;

public enum RecipeDifficultyEnum {

	HIGH("HIGH"),
	MIDDLE("MIDDLE"),
	LOW("LOW");

	private final String code;

	RecipeDifficultyEnum(String code) {
		this.code = code;
	}

	public String getCode() {
		return code;
	}
}
