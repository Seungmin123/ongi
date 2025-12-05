package com.ongi.api.recipe.web.dto;

import com.ongi.recipe.domain.enums.PageSortOptionEnum;

public record CursorPageRequest(
	Long cursor,
	Integer size,
	String sort
) {
	public static final int DEFAULT_SIZE = 20;
	public static final int MAX_SIZE = 100;

	public int resolvedSize() {
		if (size == null) return DEFAULT_SIZE;
		return Math.min(size, MAX_SIZE);
	}

	public PageSortOptionEnum resolveSort() {
		return PageSortOptionEnum.from(sort);
	}
}
