package com.ongi.recipe.domain.enums;

import java.util.Arrays;

public enum RecipeCategoryEnum {
	SOUP("SOUP", "국/탕/수프"),
	STEW("STEW", "찌개/전골"),
	RICE("RICE", "밥/덮밥/볶음밥/비빔밥"),
	NOODLE("NOODLE", "면/파스타/국수"),
	SIDE_DISH("SIDE_DISH", "반찬(볶음, 조림, 무침 등)"),
	SALAD("SALAD", "샐러드"),
	DESSERT("DESSERT", "디저트/베이킹"),
	SNACK("SNACK", "간식/분식"),
	MAIN_DISH("MAIN_DISH", "고기/생선 메인 요리"),
	SAUCE("SAUCE", "소스/드레싱"),
	DRINK("DRINK", "음료"),
	ETC("ETC", "기타");

	private final String code;

	private final String name;

	RecipeCategoryEnum(String code, String name) {
		this.code = code;
		this.name = name;
	}

	public String getCode() {
		return code;
	}

	public String getName() {
		return name;
	}

	public static RecipeCategoryEnum from(String name) {
		if(name == null || name.isBlank()) {
			return ETC;
		}

		String s = name.trim().toLowerCase();

		return Arrays.stream(values())
			.filter(v -> v.name.equalsIgnoreCase(s))
			.findFirst()
			.orElseGet(() -> ETC);
	}
}