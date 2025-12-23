package com.ongi.api.community.web.dto;

import com.ongi.api.community.adatper.out.user.UserSummary;
import java.time.LocalDateTime;
import java.util.List;

public record PostDetailResponse(
	Long postId,
	String title,
	int contentSchema,
	String contentJson,
	UserSummary author,
	List<AttachmentDto> attachments,
	long likeCount,
	long commentCount,
	long viewCount,
	LocalDateTime createdAt,
	boolean likedByMe
) {}
