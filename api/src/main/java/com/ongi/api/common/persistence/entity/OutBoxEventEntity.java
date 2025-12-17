package com.ongi.api.common.persistence.entity;

import com.ongi.api.common.persistence.enums.OutBoxAggregateTypeEnum;
import com.ongi.api.common.persistence.enums.OutBoxEventStatusEnum;
import com.ongi.api.common.persistence.enums.OutBoxEventTypeEnum;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(
	name = "outbox_event",
	indexes = {
		@Index(
			name = "uq_outbox_event_event_iduq_outbox_event_event_id",
			columnList = "event_id"
		),
		@Index(
			name = "idx_outbox_event_status_created",
			columnList = "status, created_at"
		)
	})
public class OutBoxEventEntity extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "outbox_event_id", nullable = false)
	private long id;

	@Column(name = "event_id", unique = true, nullable = false)
	private UUID eventId;

	@Enumerated(EnumType.STRING)
	@Column(name = "aggregate_type", nullable = false, length = 50, comment = "recipe, user, ingredients ...")
	private OutBoxAggregateTypeEnum aggregateType;

	@Column(name = "aggregate_id", nullable = false)
	private long aggregateId;

	@Enumerated(EnumType.STRING)
	@Column(name = "event_type", nullable = false, length = 100, comment = "recipe_create, recipe_update ...")
	private OutBoxEventTypeEnum eventType;

	@Column(name = "payload", nullable = false, comment = "실제 이벤트 데이터")
	private String payload;

	@Enumerated(EnumType.STRING)
	@Column(name = "status", nullable = false, length = 50, comment = "pending, sending, failed ...")
	private OutBoxEventStatusEnum status;

	@Column(name = "sent_at")
	private LocalDateTime sentAt;

	@ColumnDefault("0")
	@Column(name = "retry_count", nullable = false)
	private int retryCount;

	@Column(name = "last_error", comment = "Error log")
	private String lastError;

	@Column(name = "next_attempt_at", nullable = false)
	private LocalDateTime nextAttemptAt;

	@Column(name = "locked_by")
	private String lockedBy;

	@Column(name = "locked_at")
	private LocalDateTime lockedAt;

	private OutBoxEventEntity(
		UUID eventId,
		OutBoxAggregateTypeEnum aggregateType,
		Long aggregateId,
		OutBoxEventTypeEnum eventType,
		String payload,
		OutBoxEventStatusEnum status
	) {
		this.eventId = eventId;
		this.aggregateType = aggregateType;
		this.aggregateId = aggregateId;
		this.eventType = eventType;
		this.payload = payload;
		this.status = status;
		this.sentAt = LocalDateTime.now();
		this.retryCount = 0;
		this.nextAttemptAt = LocalDateTime.now();
	}

	public static OutBoxEventEntity createPending(
		UUID eventId,
		OutBoxAggregateTypeEnum aggregateType,
		Long aggregateId,
		OutBoxEventTypeEnum eventType,
		String payload
	) {
		return new OutBoxEventEntity(eventId, aggregateType, aggregateId, eventType, payload, OutBoxEventStatusEnum.PENDING);
	}

}
