package com.ongi.notification.service.adapter.out.persistence;

import com.ongi.notification.domain.enums.NotificationStatus;
import com.ongi.notification.domain.enums.NotificationType;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table("notification")
public class NotificationEntity {

	@Id
	private Long id;

	private Long userId;

	private NotificationType type;

	private String title;

	private String content;

	private NotificationStatus status;

	private String eventId;

	private LocalDateTime createdAt;

	@Builder
	public NotificationEntity(Long id, Long userId, NotificationType type, String title, String content, String eventId, NotificationStatus status, LocalDateTime createdAt) {
		this.id = id;
		this.userId = userId;
		this.type = type;
		this.title = title;
		this.content = content;
		this.eventId = eventId;
		this.status = (status == null) ? NotificationStatus.PENDING : status;
		this.createdAt = (createdAt == null) ? LocalDateTime.now() : createdAt;
	}

	public NotificationEntity markSent() {
		return NotificationEntity.builder()
			.id(this.id)
			.userId(this.userId)
			.type(this.type)
			.title(this.title)
			.content(this.content)
			.eventId(this.eventId)
			.status(NotificationStatus.SENT)
			.createdAt(this.createdAt)
			.build();
	}

	public NotificationEntity markFailed() {
		return NotificationEntity.builder()
			.id(this.id)
			.userId(this.userId)
			.type(this.type)
			.title(this.title)
			.content(this.content)
			.eventId(this.eventId)
			.status(NotificationStatus.FAILED)
			.createdAt(this.createdAt)
			.build();
	}
}
