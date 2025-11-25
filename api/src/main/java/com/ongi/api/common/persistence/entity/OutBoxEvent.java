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
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "outbox_event")
public class OutBoxEvent extends BaseTimeEntity{

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "outbox_event_id", nullable = false)
	private Long id;

	@Enumerated(EnumType.STRING)
	@Column(name = "aggregate_type", nullable = false, comment = "recipe, user, ingredients ...")
	private OutBoxAggregateTypeEnum aggregateType;

	@Enumerated(EnumType.STRING)
	@Column(name = "event_type", nullable = false, comment = "recipe_create, recipe_update ...")
	private OutBoxEventTypeEnum eventType;

	@Column(name = "payload", nullable = false, comment = "실제 이벤트 데이터")
	private String payload;

	@Enumerated(EnumType.STRING)
	@Column(name = "status", nullable = false, comment = "pending, sending, failed ...")
	private OutBoxEventStatusEnum status;

	@Column(name = "last_error", comment = "Error log")
	private String lastError;

	private OutBoxEvent(
		OutBoxAggregateTypeEnum aggregateType,
		OutBoxEventTypeEnum eventType,
		String payload,
		OutBoxEventStatusEnum status
	) {
		this.aggregateType = aggregateType;
		this.eventType = eventType;
		this.payload = payload;
		this.status = status;
	}

	public static OutBoxEvent createPending(
		OutBoxAggregateTypeEnum aggregateType,
		OutBoxEventTypeEnum eventType,
		String payload
	) {
		return new OutBoxEvent(aggregateType, eventType, payload, OutBoxEventStatusEnum.PENDING);
	}

	public void markSending() {
		if(this.status == OutBoxEventStatusEnum.SUCCESS) {
			return;
		}
		this.status = OutBoxEventStatusEnum.SENDING;
	}

	public void markFailed() {
		this.status = OutBoxEventStatusEnum.FAILED;
	}

	public void markSuccess() {
		this.status = OutBoxEventStatusEnum.SUCCESS;
	}

	public void setError(String error) {
		this.lastError = error;
	}
}
