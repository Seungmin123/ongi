package com.ongi.ingredients.domain.enums;


public enum IngredientCategoryEnum {

	VEGETABLE("VEGETABLE", "채소"),
	FRUIT("FRUIT", "과일"),
	MEAT("MEAT", "육류"),
	SEAFOOD("SEAFOOD", "해산물"),
	GRAIN("GRAIN", "곡류/빵/면"),
	DAIRY_EGG("DAIRY_EGG", "유제품/달걀"),
	SEASONING("SEASONING", "조미료/양념"),
	OIL_FAT("OIL_FAT", "기름/지방"),
	NUTS("NUTS", "견과류"),
	BEVERAGE("BEVERAGE", "음료"),
	SAUCE("SAUCE", "소스"),
	FROZEN("FROZEN", "냉동식품"),
	OTHER("OTHER", "기타");

	private final String code;
	private final String description;

	IngredientCategoryEnum(String code, String description) {
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
