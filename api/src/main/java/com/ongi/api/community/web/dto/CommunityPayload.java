package com.ongi.api.community.web.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record CommunityPayload (
	UUID eventId,
	long userId,
	long postId,
	Long commentId,
	String eventType,
	LocalDateTime occurredAt
) {

}
