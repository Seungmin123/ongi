package com.ongi.api.recipe.web.dto;

import com.ongi.recipe.domain.Recipe;
import com.ongi.recipe.domain.enums.RecipeCategoryEnum;
import com.ongi.recipe.domain.enums.RecipeDifficultyEnum;

public record RecipeCacheValue(
	Long id,
	Long authorId,
	String title,
	String description,
	Double serving,
	Integer cookingTimeMin,
	RecipeDifficultyEnum difficulty,
	String imageUrl,
	String videoUrl,
	String source,
	RecipeCategoryEnum category
) {
	public static RecipeCacheValue from(Recipe r) {
		return new RecipeCacheValue(
			r.getId(), r.getAuthorId(), r.getTitle(), r.getDescription(),
			r.getServing(), r.getCookingTimeMin(), r.getDifficulty(),
			r.getImageUrl(), r.getVideoUrl(), r.getSource(), r.getCategory()
		);
	}

	public Recipe toDomain() {
		return Recipe.create(
			id, authorId, title, description,
			serving, cookingTimeMin, difficulty,
			imageUrl, videoUrl, source, category
		);
	}
}
