package com.ongi.ingredients.domain;

import com.ongi.ingredients.domain.enums.NutritionEnum;
import com.ongi.ingredients.domain.enums.NutritionUnitEnum;

public class Nutrition {

	private Long id;

	private NutritionEnum code;

	private String displayName;

	private NutritionUnitEnum unit;

	private Nutrition(
		NutritionEnum code,
		NutritionUnitEnum unit
	) {
		this.code = code;
		this.displayName = code.getDisplayName();
		this.unit = unit;
	}

	public static Nutrition create(
		NutritionEnum nutritionEnum,
		NutritionUnitEnum unit
	) {
		return new Nutrition(nutritionEnum, unit);
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public NutritionEnum getCode() {
		return code;
	}

	public void setCode(NutritionEnum code) {
		this.code = code;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public NutritionUnitEnum getUnit() {
		return unit;
	}

	public void setUnit(NutritionUnitEnum unit) {
		this.unit = unit;
	}
}
