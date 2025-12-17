package com.ongi.api.common.persistence.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum OutBoxEventTypeEnum {

	RECIPE_LIKED("RECIPE_LIKED"),
	RECIPE_UNLIKED("RECIPE_UNLIKED"),
	RECIPE_VIEW("RECIPE_VIEW"),
	RECIPE_CREATED("RECIPE_CREATED"),
	RECIPE_UPDATED("RECIPE_UPDATED");

	private final String code;
}
