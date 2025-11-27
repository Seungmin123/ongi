package com.ongi.ingredients.domain.enums;


public enum IngredientCategoryEnum {

	// 곡류/밥/빵/면/떡/감자 등 주식 계열
	GRAIN_RICE("GRAIN_RICE", "밥/잡곡밥/곡류"),
	GRAIN_NOODLE_DUMPLING("GRAIN_NOODLE_DUMPLING", "면/만두"),
	GRAIN_BREAD_SNACK("GRAIN_BREAD_SNACK", "빵/과자/떡계열"),
	GRAIN_PORRIDGE_SOUP("GRAIN_PORRIDGE_SOUP", "죽/스프"),
	ROOT_POTATO_STARCH("ROOT_POTATO_STARCH", "감자/전분"),

	// 육류 계열
	MEAT_RAW("MEAT_RAW", "생육/정육"),
	MEAT_PROCESSED("MEAT_PROCESSED", "식육가공품/동물성가공"),

	// 수산물 계열
	SEAFOOD_RAW("SEAFOOD_RAW", "어패류/생선/수산물"),
	SEAFOOD_PROCESSED("SEAFOOD_PROCESSED", "수산가공품/젓갈"),

	// 채소/버섯/김치/해조류
	VEGETABLE_FRESH("VEGETABLE_FRESH", "신선채소/샐러드/나물"),
	VEGETABLE_PICKLED_KIMCHI("VEGETABLE_PICKLED_KIMCHI", "김치/장아찌/절임"),
	VEGETABLE_SEAWEED("VEGETABLE_SEAWEED", "해조류/김"),
	MUSHROOM("MUSHROOM", "버섯류"),

	// 과일
	FRUIT("FRUIT", "과일"),

	// 두류/견과/종실/두부·묵
	LEGUME("LEGUME", "두류"),
	NUT_SEED("NUT_SEED", "견과/종실"),
	TOFU_JELLY("TOFU_JELLY", "두부/묵"),

	// 유제품/계란
	DAIRY("DAIRY", "우유/유제품/치즈/요구르트"),
	EGG("EGG", "계란/난류/알가공품"),

	// 유지/식용유
	OIL_FAT("OIL_FAT", "유지/식용유"),

	// 당류/디저트류
	SWEET_SUGAR("SWEET_SUGAR", "당류/설탕/시럽"),
	SWEET_JAM("SWEET_JAM", "잼/스프레드"),
	SWEET_CHOCOLATE("SWEET_CHOCOLATE", "코코아/초콜릿"),
	SWEET_ICE("SWEET_ICE", "빙과/아이스크림"),

	// 조미료/장류/소스
	SEASONING("SEASONING", "조미료/양념"),
	SAUCE_PASTE("SAUCE_PASTE", "장류/소스/페이스트"),

	// 완성 요리/즉석식품
	READY_SOUP_STEW("READY_SOUP_STEW", "국/탕/찌개/전골"),
	READY_MAIN_SIDE("READY_MAIN_SIDE", "구이/전/부침/튀김/볶음/조림"),
	READY_MEAL("READY_MEAL", "즉석식품/도시락/가공반찬"),

	// 음료/주류
	BEVERAGE_SOFT("BEVERAGE_SOFT", "일반 음료/청량음료"),
	BEVERAGE_TEA("BEVERAGE_TEA", "차/커피"),
	BEVERAGE_ALCOHOL("BEVERAGE_ALCOHOL", "주류"),

	// 특수 용도 식품
	SPECIAL_MEDICAL("SPECIAL_MEDICAL", "특수의료용도식품"),
	SPECIAL_NUTRITION("SPECIAL_NUTRITION", "특수영양식품"),

	// 기타
	OTHER("OTHER", "기타/분류불명");

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
