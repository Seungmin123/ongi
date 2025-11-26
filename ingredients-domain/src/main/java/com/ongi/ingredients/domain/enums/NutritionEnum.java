package com.ongi.ingredients.domain.enums;

public enum NutritionEnum {

	// 🔥 주요 4대 영양소 (Macronutrients)
	ENERGY_KCAL("ENERGY_KCAL", "열량(kcal)", NutritionUnitEnum.KCAL),
	PROTEIN("PROTEIN", "단백질", NutritionUnitEnum.G),
	FAT("FAT", "지방", NutritionUnitEnum.G),
	CARBOHYDRATE("CARBOHYDRATE", "탄수화물", NutritionUnitEnum.G),

	// 세부 Macro
	SUGAR("SUGAR", "당류", NutritionUnitEnum.G),
	DIETARY_FIBER("DIETARY_FIBER", "식이섬유", NutritionUnitEnum.G),
	SATURATED_FAT("SATURATED_FAT", "포화지방", NutritionUnitEnum.G),
	TRANS_FAT("TRANS_FAT", "트랜스지방", NutritionUnitEnum.G),
	CHOLESTEROL("CHOLESTEROL", "콜레스테롤", NutritionUnitEnum.MG),
	SODIUM("SODIUM", "나트륨", NutritionUnitEnum.MG),

	// 🔥 미량 영양소 (Micronutrients)
	CALCIUM("CALCIUM", "칼슘", NutritionUnitEnum.MG),
	IRON("IRON", "철분", NutritionUnitEnum.MG),
	POTASSIUM("POTASSIUM", "칼륨", NutritionUnitEnum.MG),
	MAGNESIUM("MAGNESIUM", "마그네슘", NutritionUnitEnum.MG),
	PHOSPHORUS("PHOSPHORUS", "인", NutritionUnitEnum.MG),

	VITAMIN_A("VITAMIN_A", "비타민 A", NutritionUnitEnum.MCG),
	VITAMIN_B1("VITAMIN_B1", "비타민 B1", NutritionUnitEnum.MG),
	VITAMIN_B2("VITAMIN_B2", "비타민 B2", NutritionUnitEnum.MG),
	VITAMIN_B3("VITAMIN_B3", "니아신", NutritionUnitEnum.MG),
	VITAMIN_C("VITAMIN_C", "비타민 C", NutritionUnitEnum.MG),
	VITAMIN_D("VITAMIN_D", "비타민 D", NutritionUnitEnum.MCG),
	VITAMIN_E("VITAMIN_E", "비타민 E", NutritionUnitEnum.MG),
	VITAMIN_K("VITAMIN_K", "비타민 K", NutritionUnitEnum.MCG);

	private final String code;
	private final String displayName;
	private final NutritionUnitEnum unit;

	NutritionEnum(String code, String displayName, NutritionUnitEnum unit) {
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

	public NutritionUnitEnum getUnit() {
		return unit;
	}
}
