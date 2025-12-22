package com.ongi.api.user.adapter.out.cache.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "user_processed_event")
public class UserProcessedEventEntity {

	@Id
	@Column(name = "event_id", nullable = false)
	public UUID eventId;

	@Column(name = "processed_at")
	public LocalDateTime processedAt;

	private UserProcessedEventEntity(
		UUID eventId
	) {
		this.eventId = eventId;
		this.processedAt = LocalDateTime.now();
	}

	public static UserProcessedEventEntity create(UUID eventId) {
		return new UserProcessedEventEntity(eventId);
	}

	public void processed() {
		this.processedAt = LocalDateTime.now();
	}

}
