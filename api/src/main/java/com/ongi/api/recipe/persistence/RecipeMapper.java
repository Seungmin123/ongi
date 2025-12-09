package com.ongi.api.recipe.persistence;

import com.ongi.recipe.domain.Recipe;
import com.ongi.recipe.domain.RecipeSteps;
import com.ongi.recipe.domain.RecipeTags;

public class RecipeMapper {

	public static RecipeEntity toEntity(Recipe recipe) {
		if(recipe.getId() == null) {
			return RecipeEntity.builder()
				.title(recipe.getTitle())
				.description(recipe.getDescription())
				.serving(recipe.getServing())
				.cookingTimeMin(recipe.getCookingTimeMin())
				.difficulty(recipe.getDifficulty())
				.imageUrl(recipe.getImageUrl())
				.videoUrl(recipe.getVideoUrl())
				.source(recipe.getSource())
				.category(recipe.getCategory())
				.build();
		} else {
			return RecipeEntity.builder()
				.id(recipe.getId())
				.title(recipe.getTitle())
				.description(recipe.getDescription())
				.serving(recipe.getServing())
				.cookingTimeMin(recipe.getCookingTimeMin())
				.difficulty(recipe.getDifficulty())
				.imageUrl(recipe.getImageUrl())
				.videoUrl(recipe.getVideoUrl())
				.source(recipe.getSource())
				.category(recipe.getCategory())
				.build();
		}
	}

	public static Recipe toDomain(RecipeEntity entity) {
		return Recipe.create(entity.getId(), entity.getTitle(), entity.getDescription(), entity.getServing(), entity.getCookingTimeMin(), entity.getDifficulty(), entity.getImageUrl(),
			entity.getVideoUrl(), entity.getSource(), entity.getCategory());
	}

	public static RecipeStepsEntity toEntity(RecipeSteps steps) {
		if(steps.getId() == null) {
			return RecipeStepsEntity.builder()
				.recipeId(steps.getRecipeId())
				.stepOrder(steps.getStepOrder())
				.title(steps.getTitle())
				.description(steps.getDescription())
				.estimatedMin(steps.getEstimatedMin())
				.waitMin(steps.getWaitMin())
				.temperature(steps.getTemperature())
				.imageUrl(steps.getImageUrl())
				.videoUrl(steps.getVideoUrl())
				.build();
		} else {
			return RecipeStepsEntity.builder()
				.id(steps.getId())
				.recipeId(steps.getRecipeId())
				.stepOrder(steps.getStepOrder())
				.title(steps.getTitle())
				.description(steps.getDescription())
				.estimatedMin(steps.getEstimatedMin())
				.waitMin(steps.getWaitMin())
				.temperature(steps.getTemperature())
				.imageUrl(steps.getImageUrl())
				.videoUrl(steps.getVideoUrl())
				.build();
		}
	}

	public static RecipeSteps toDomain(RecipeStepsEntity entity) {
		return RecipeSteps.create(entity.getId(), entity.getRecipeId(), entity.getStepOrder(), entity.getTitle(),
			entity.getDescription(), entity.getEstimatedMin(), entity.getWaitMin(),
			entity.getTemperature(), entity.getImageUrl(), entity.getVideoUrl());
	}

	public static RecipeTagsEntity toEntity(RecipeTags recipeTags) {
		if(recipeTags.getId() == null) {
			return RecipeTagsEntity.builder()
				.recipeId(recipeTags.getRecipeId())
				.tag(recipeTags.getTag())
				.build();
		} else {
			return RecipeTagsEntity.builder()
				.id(recipeTags.getId())
				.recipeId(recipeTags.getRecipeId())
				.tag(recipeTags.getTag())
				.build();
		}
	}

	public static RecipeTags toDomain(RecipeTagsEntity entity) {
		return RecipeTags.create(entity.getId(), entity.getRecipeId(), entity.getTag());
	}
}
