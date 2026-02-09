package com.ongi.notification.domain;

import com.ongi.notification.domain.enums.NotificationType;

public record BroadcastRequest(
    String targetGroup,
    NotificationType type,
    String title,
    String content
) {}
