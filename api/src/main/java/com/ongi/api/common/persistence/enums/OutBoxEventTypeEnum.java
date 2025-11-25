package com.ongi.api.common.persistence.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum OutBoxEventTypeEnum {

	RECIPE_CREATED("RECIPE_CREATED"),
	RECIPE_UPDATED("RECIPE_UPDATED");

	private final String code;
}
