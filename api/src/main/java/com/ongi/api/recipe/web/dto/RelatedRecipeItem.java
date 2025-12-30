package com.ongi.api.recipe.web.dto;

public record RelatedRecipeItem(
	Long recipeId,
	String title,
	String imageUrl,
	Integer cookingTimeMin,
	String difficulty,
	String category,
	Double score
) {

}
