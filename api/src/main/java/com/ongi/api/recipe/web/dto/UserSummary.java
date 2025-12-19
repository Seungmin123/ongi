package com.ongi.api.recipe.web.dto;

public record UserSummary(
	Long userId,
	String nickname,
	String profileUrl
) {}
