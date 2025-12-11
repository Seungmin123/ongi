package com.ongi.api.recipe.web.dto;

public record RecipeStepCreateRequest(
	String description,
	String imageUrl
) {

}
