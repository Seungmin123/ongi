package com.ongi.api.recipe.web.dto;

public record RecipeCardResponse(
	Long id,
	String title,
	String imageUrl,
	Integer cookTime,
	String cookTimeText,
	Integer servings,
	String difficulty,
	Double rating,
	String category,
	Integer likes,
	Integer comments
) {

}
