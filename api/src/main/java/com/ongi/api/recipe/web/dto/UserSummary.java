package com.ongi.api.recipe.web.dto;

public record UserSummary(
	Long userId,
	String displayName,
	String profileUrl
) {}
