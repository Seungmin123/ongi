package com.ongi.api.community.web.dto;

import java.time.LocalDateTime;

public record PostDetailRow(
	Long postId,
	Long authorId,
	String title,
	int contentSchema,
	String contentJson,
	long likeCount,
	long commentCount,
	long viewCount,
	LocalDateTime createdAt
) {}
