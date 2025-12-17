package com.ongi.api.common.persistence.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum OutBoxEventStatusEnum {


	PENDING("PENDING"),
	PROCESSING("PROCESSING"),
	SENDING("SENDING"),
	FAILED("FAILED"),
	SUCCESS("SUCCESS");

	private final String code;
}
