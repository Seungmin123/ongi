package com.ongi.api.recipe.web.dto;

public record CommentCreateResponse(
	Long commentId,
	Long recipeId,
	Long commentCount
) {}
