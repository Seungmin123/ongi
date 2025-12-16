package com.ongi.api.recipe.web.dto;

import java.util.List;

public record RecipeDetailResponse(
	String recipeImageUrl,
	String title,
	Integer cookTime,
	String cookTimeText,
	Integer servings,
	String difficulty,
	Long likeCount,
	Long commentCount,

	List<RecipeIngredientResponse> ingredients,
	List<RecipeStepsResponse> recipeSteps

	// TODO Community 관련 기능 추가 시 등록
	/*List<Comment> comments,
	Integer commentsCount*/
) {
}
