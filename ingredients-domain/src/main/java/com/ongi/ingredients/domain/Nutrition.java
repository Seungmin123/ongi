package com.ongi.ingredients.domain;

import com.ongi.ingredients.domain.enums.NutritionEnum;

public class Nutrition {

	private Long id;

	private NutritionEnum code;

	private String displayName;

	private String unit;

	private Nutrition(
		NutritionEnum code
	) {
		this.code = code;
		this.displayName = code.getDisplayName();
		this.unit = code.getUnit();
	}

	public static Nutrition create(
		NutritionEnum nutritionEnum
	) {
		return new Nutrition(nutritionEnum);
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

	public String getUnit() {
		return unit;
	}

	public void setUnit(String unit) {
		this.unit = unit;
	}
}
