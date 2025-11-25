package com.ongi.ingredients.domain.enums;

public enum RecipeIngredientUnitEnum {

	// 용량 단위
	ML("ML", "ml"),
	L("L", "L"),

	// 무게 단위
	G("G", "g"),
	KG("KG", "kg"),

	// 계량 단위(요리에서 자주 사용)
	TBSP("TBSP", "큰술"),       // tablespoon
	TSP("TSP", "작은술"),       // teaspoon
	CUP("CUP", "컵"),

	// 개수 단위
	PIECE("PIECE", "개"),
	PACK("PACK", "팩"),
	BUNCH("BUNCH", "줌/다발"),

	// 계량이 애매한 경우
	DASH("DASH", "약간"),
	PINCH("PINCH", "꼬집"),
	TO_TASTE("TO_TASTE", "기호에 맞게"),

	// 온도 단위
	CELSIUS("CELSIUS", "°C"),

	// 길이/사이즈
	SLICE("SLICE", "슬라이스"),
	SHEET("SHEET", "장");

	private final String code;
	private final String description;

	RecipeIngredientUnitEnum(String code, String description) {
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
