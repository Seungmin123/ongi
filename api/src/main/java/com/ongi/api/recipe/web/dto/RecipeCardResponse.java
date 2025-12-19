package com.ongi.api.recipe.web.dto;

public record RecipeCardResponse(
	Long id,
	String title,
	String imageUrl,
	Integer cookTime,
	String cookTimeText,
	Integer servings,
	String difficulty,
	Long likeCount,
	Long commentsCount,
	Long bookmarkCount,
	String category
) {

}
