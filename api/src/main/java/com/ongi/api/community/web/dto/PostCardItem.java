package com.ongi.api.community.web.dto;

import com.ongi.api.community.adatper.out.user.UserSummary;
import java.time.LocalDateTime;

public record PostCardItem(
	Long postId,
	String title,
	String contentText,
	UserSummary author,
	AttachmentDto cover,
	long likeCount,
	long commentCount,
	long viewCount,
	LocalDateTime createdAt,
	boolean liked
) {}
