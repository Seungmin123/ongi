package com.ongi.api.community.web.dto;

import com.ongi.api.community.adatper.out.persistence.enums.CommentStatus;
import com.ongi.api.community.adatper.out.user.UserSummary;
import java.time.LocalDateTime;

public record CommentItem(
	Long commentId,
	Long rootId,
	Long parentId,
	int depth,
	String content,
	UserSummary author,
	LocalDateTime createdAt,
	boolean liked
) {

}
