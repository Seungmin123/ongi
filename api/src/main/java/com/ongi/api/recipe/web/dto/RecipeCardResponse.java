package com.ongi.api.recipe.web.dto;

public record RecipeCardResponse(
	Long id,
	String title,
	String imageUrl,
	String cookTime,
	Integer servings,
	String difficulty,
	Double rating,
	String category,
	Integer likes,
	Integer comments
) {

}
