package com.ongi.api.common.persistence.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum OutBoxAggregateTypeEnum {

	RECIPE("RECIPE"),
	INGREDIENT("INGREDIENT"),
	USER("USER");

	private final String code;
}
