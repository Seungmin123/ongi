package com.ongi.api.recipe.web.dto;

public record RecipeUserFlags(
	boolean liked,
	boolean saved
) {
}
