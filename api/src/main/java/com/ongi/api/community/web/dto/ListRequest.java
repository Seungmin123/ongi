package com.ongi.api.community.web.dto;

import com.ongi.api.community.adatper.out.persistence.enums.CommentSortOption;
import com.ongi.api.community.adatper.out.persistence.enums.PostSortOption;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public record ListRequest(

	@Min(0)
	Integer page,

	@Min(1)
	@Max(100)
	Integer size,

	String sort
) {

	private static final int DEFAULT_PAGE = 0;
	private static final int DEFAULT_SIZE = 20;
	private static final int MAX_SIZE = 100;

	public int resolvedPage() {
		return page == null ? DEFAULT_PAGE : page;
	}

	public int resolvedSize() {
		if (size == null) return DEFAULT_SIZE;
		return Math.min(size, MAX_SIZE);
	}

	public PostSortOption resolvePostSort() {
		return PostSortOption.from(sort);
	}

	public CommentSortOption resolveCommentSort() {
		return CommentSortOption.from(sort);
	}
}
