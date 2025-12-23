package com.ongi.api.community.web.dto;

import com.ongi.api.community.adatper.out.persistence.enums.CommentStatus;
import java.time.LocalDateTime;

public record CommentRow(
	Long commentId,
	Long rootId,
	Long parentId,
	int depth,
	Long userId,
	String content,
	CommentStatus status,
	LocalDateTime createdAt
) {}
