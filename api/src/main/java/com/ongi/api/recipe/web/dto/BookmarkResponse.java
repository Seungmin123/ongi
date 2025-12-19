package com.ongi.api.recipe.web.dto;

public record BookmarkResponse(
	boolean bookmarkedByMe,
	long bookmarkCount
) {}
