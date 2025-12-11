package com.ongi.api.recipe.web.dto;

import com.ongi.recipe.domain.enums.RecipeCategoryEnum;
import com.ongi.recipe.domain.enums.RecipeDifficultyEnum;
import java.util.List;

public record RecipeUpsertRequest(
	Long userId,
	Long recipeId,
	String title,
	String description,
	RecipeCategoryEnum category,
	RecipeDifficultyEnum difficulty,
	Integer cookingTimeMin,
	Double serving,
	List<RecipeIngredientCreateRequest> ingredients,
	String imageUrl,
	List<RecipeStepCreateRequest> steps
) {

}
