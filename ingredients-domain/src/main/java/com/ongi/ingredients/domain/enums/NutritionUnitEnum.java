package com.ongi.ingredients.domain.enums;

public enum NutritionUnitEnum {

	KCAL("kcal", "킬로칼로리"),
	G("g", "그램"),
	MG("mg", "밀리그램"),
	MCG("µg", "마이크로그램");

	private final String code;        // DB or API에 저장될 단위값
	private final String description; // UI용 설명

	NutritionUnitEnum(String code, String description) {
		this.code = code;
		this.description = description;
	}

	public String getCode() {
		return code;
	}

	public String getDescription() {
		return description;
	}
}
