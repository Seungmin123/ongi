package com.ongi.ingredients.domain.enums;


public enum NutritionBasisEnum {

	PER_100G("PER_100G", "100g"),
	PER_1G("PER_1G", "1g"),
	PER_PIECE("PER_PIECE", "조각"),
	PER_SERVING("PER_SERVING", "인분"),
	PER_100ML("PER_100ML", "100ml");

	private final String code;
	private final String description;

	NutritionBasisEnum(String code, String description) {
		this.code = code;
		this.description = description;
	}

	public String getCode() {
		return this.code;
	}

	public String getDescription() {
		return this.description;
	}
}
