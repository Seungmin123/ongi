package com.ongi.api.recipe.web.dto;

import com.ongi.ingredients.domain.enums.RecipeIngredientUnitEnum;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record RecipeIngredientCreateRequest(
	Long ingredientId,
	@NotBlank
	String name,
	@NotNull @Positive
	Double quantity,
	RecipeIngredientUnitEnum unit,
	String note
) {

}
