package com.ongi.ingredients.domain.enums;

public enum NutritionEnum {

	// 🔥 주요 4대 영양소 (Macronutrients)
	ENERGY_KCAL("ENERGY_KCAL", "열량(kcal)", "kcal"),
	PROTEIN("PROTEIN", "단백질", "g"),
	FAT("FAT", "지방", "g"),
	CARBOHYDRATE("CARBOHYDRATE", "탄수화물", "g"),

	// 세부 Macro
	SUGAR("SUGAR", "당류", "g"),
	DIETARY_FIBER("DIETARY_FIBER", "식이섬유", "g"),
	SATURATED_FAT("SATURATED_FAT", "포화지방", "g"),
	TRANS_FAT("TRANS_FAT", "트랜스지방", "g"),
	CHOLESTEROL("CHOLESTEROL", "콜레스테롤", "mg"),
	SODIUM("SODIUM", "나트륨", "mg"),

	// 🔥 미량 영양소 (Micronutrients) — 필요시 확장
	CALCIUM("CALCIUM", "칼슘", "mg"),
	IRON("IRON", "철분", "mg"),
	POTASSIUM("POTASSIUM", "칼륨", "mg"),
	MAGNESIUM("MAGNESIUM", "마그네슘", "mg"),
	PHOSPHORUS("PHOSPHORUS", "인", "mg"),

	VITAMIN_A("VITAMIN_A", "비타민 A", "µg"),
	VITAMIN_B1("VITAMIN_B1", "비타민 B1", "mg"),
	VITAMIN_B2("VITAMIN_B2", "비타민 B2", "mg"),
	VITAMIN_B3("VITAMIN_B3", "니아신", "mg"),
	VITAMIN_C("VITAMIN_C", "비타민 C", "mg"),
	VITAMIN_D("VITAMIN_D", "비타민 D", "µg"),
	VITAMIN_E("VITAMIN_E", "비타민 E", "mg"),
	VITAMIN_K("VITAMIN_K", "비타민 K", "µg");

	private final String code;
	private final String displayName;
	private final String unit;

	NutritionEnum(String code, String displayName, String unit) {
		this.code = code;
		this.displayName = displayName;
		this.unit = unit;
	}

	public String getCode() {
		return code;
	}

	public String getDisplayName() {
		return displayName;
	}

	public String getUnit() {
		return unit;
	}
}
