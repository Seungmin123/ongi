package com.ongi.api.recipe.web.dto;

import com.ongi.recipe.domain.enums.RecipeCommentStatus;
import java.time.LocalDateTime;

public record CommentRow(
	Long commentId,
	Long rootId,
	Long parentId,
	int depth,
	Long userId,
	String content,
	RecipeCommentStatus status,
	LocalDateTime createdAt
) {}
