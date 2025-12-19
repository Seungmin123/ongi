package com.ongi.api.recipe.web.dto;

import com.ongi.recipe.domain.enums.CommentSortOption;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record CommentPageRequest(
	@Min(0) Integer page,
	@Min(1) @Max(100) Integer size,
	String sort
) {
	public CommentPageRequest {
		page = page == null ? 0 : page;
		size = size == null ? 20 : Math.min(size, 100);
		sort = sort == null ? "OLDEST" : sort;
	}

	public CommentSortOption resolveSort() {
		return CommentSortOption.from(sort);
	}
}
