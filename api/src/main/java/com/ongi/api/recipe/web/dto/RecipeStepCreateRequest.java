package com.ongi.api.recipe.web.dto;

import jakarta.validation.constraints.NotBlank;

public record RecipeStepCreateRequest(
	@NotBlank
	String description,
	String imageUrl
) {

}
