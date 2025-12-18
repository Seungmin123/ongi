package com.ongi.api.recipe.web.dto;

public record CommentDeleteResponse(
	Long commentId,
	Long recipeId,
	Long commentCount
) {}
