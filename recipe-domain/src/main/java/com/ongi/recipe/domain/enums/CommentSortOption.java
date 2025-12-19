package com.ongi.recipe.domain.enums;

public enum CommentSortOption {

	LATEST,
	OLDEST;

	public static CommentSortOption from(String value) {
		if (value == null || value.isBlank()) return OLDEST;
		return switch (value.toUpperCase()) {
			case "NEWEST" -> LATEST;
			case "OLDEST" -> OLDEST;
			default -> throw new IllegalArgumentException("Invalid sort: " + value);
		};
	}
}
