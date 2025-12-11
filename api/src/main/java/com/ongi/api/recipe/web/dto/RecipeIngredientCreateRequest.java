package com.ongi.api.recipe.web.dto;

import com.ongi.ingredients.domain.enums.RecipeIngredientUnitEnum;

public record RecipeIngredientCreateRequest(
	Long ingredientId,
	String name,
	Double quantity,
	RecipeIngredientUnitEnum unit,
	String note
) {

}
