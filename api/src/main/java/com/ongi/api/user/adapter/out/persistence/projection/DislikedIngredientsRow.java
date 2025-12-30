package com.ongi.api.user.adapter.out.persistence.projection;

import com.ongi.ingredients.domain.enums.IngredientCategoryEnum;

public record DislikedIngredientsRow(
	Long id,
	String code,
	String name,
	IngredientCategoryEnum category
) {}