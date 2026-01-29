package com.ongi.api.recipe.web.dto;

import com.ongi.recipe.domain.enums.RecipeCategoryEnum;
import com.ongi.recipe.domain.enums.RecipeDifficultyEnum;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record RecipeUpsertRequest(
	Long recipeId,
	@NotBlank
	String title,
	String description,
	@NotNull
	RecipeCategoryEnum category,
	RecipeDifficultyEnum difficulty,
	Integer cookingTimeMin,
	Double serving,
	@NotEmpty @Valid
	List<RecipeIngredientCreateRequest> ingredients,
	String imageUrl,
	@NotEmpty @Valid
	List<RecipeStepCreateRequest> steps
) {

}
