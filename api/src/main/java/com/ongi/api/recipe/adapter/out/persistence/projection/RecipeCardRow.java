package com.ongi.api.recipe.adapter.out.persistence.projection;

public record RecipeCardRow(
	Long recipeId,
	String title,
	String imageUrl,
	Integer cookingTimeMin,
	String difficulty,
	String category
) {

}
