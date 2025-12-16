package com.ongi.api.recipe.web.dto;

public record RecipeDetailResponse(
	RecipeDetailBaseResponse detail,
	boolean likedByMe,
	boolean savedByMe
) {
}
