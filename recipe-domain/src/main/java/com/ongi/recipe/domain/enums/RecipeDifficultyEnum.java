package com.ongi.recipe.domain.enums;

public enum RecipeDifficultyEnum {

	HIGH("고급"),
	MEDIUM("중급"),
	LOW("초급");

	private final String code;

	RecipeDifficultyEnum(String code) {
		this.code = code;
	}

	public String getCode() {
		return code;
	}
}
