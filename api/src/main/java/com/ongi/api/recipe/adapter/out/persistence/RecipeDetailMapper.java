package com.ongi.api.recipe.adapter.out.persistence;

import com.ongi.api.recipe.web.dto.RecipeIngredientResponse;
import com.ongi.api.recipe.web.dto.RecipeStepsResponse;
import com.ongi.ingredients.domain.RecipeIngredient;
import com.ongi.recipe.domain.RecipeSteps;

public class RecipeDetailMapper {

	public static RecipeIngredientResponse toIngredientResponse(RecipeIngredient ing) {
		return new RecipeIngredientResponse(
			ing.getIngredientId(),
			ing.getIngredient() != null ? ing.getIngredient().getName() : null,
			ing.getQuantity(),
			ing.getUnit() != null ? ing.getUnit().getDescription() : null,
			ing.getNote()
		);
	}

	public static RecipeStepsResponse toStepsResponse(RecipeSteps step) {
		return new RecipeStepsResponse(
			step.getId(),
			step.getStepOrder(),
			step.getTitle(),
			step.getDescription(),
			step.getEstimatedMin(),
			step.getWaitMin(),
			step.getTemperature(),
			step.getImageUrl(),
			step.getVideoUrl()
		);
	}
}
