package com.ongi.api.recipe.web.dto;

import java.time.LocalDateTime;

public record RecipeCommentItem(
	Long commentId,
	Long rootId,
	Long parentId,
	int depth,
	String content,
	UserSummary userSummary,
	LocalDateTime createdAt
) {}
