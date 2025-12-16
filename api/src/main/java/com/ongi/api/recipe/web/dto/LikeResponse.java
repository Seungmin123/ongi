package com.ongi.api.recipe.web.dto;

public record LikeResponse(
	boolean likedByMe,
	long likeCount
) {}
