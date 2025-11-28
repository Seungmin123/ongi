package com.ongi.api.admin.web.dto;

import com.ongi.ingredients.domain.enums.RecipeIngredientUnitEnum;

public record ParsedIngredient(
	String name, // 재료명 (예: 연두부)
	Double quantity, // 수량 (예: 75)
	RecipeIngredientUnitEnum unit, // 단위 (예: G)
	String note // 비고 (예: 3/4모, 다진 것 등)
) { }
