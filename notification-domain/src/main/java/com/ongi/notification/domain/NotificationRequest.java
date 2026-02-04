package com.ongi.notification.domain;

import com.ongi.notification.domain.enums.NotificationType;
import java.util.Map;

public record NotificationRequest(
	Long userId,
	NotificationType type,
	String title,
	String content,
	String eventId,
	Map<String, String> extraData
) {
}