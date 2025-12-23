package com.ongi.api.community.web.dto;

import java.time.LocalDateTime;

public record PostCardRow(
	Long postId,
	Long authorId,
	String title,
	String contentText,      // 리스트 요약
	Long coverAttachmentId,  // 커버 1장만
	long likeCount,
	long commentCount,
	long viewCount,
	LocalDateTime createdAt
) {}
