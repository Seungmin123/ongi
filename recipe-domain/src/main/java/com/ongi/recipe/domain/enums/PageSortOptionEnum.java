package com.ongi.recipe.domain.enums;

import java.util.Arrays;

public enum PageSortOptionEnum {

	CREATED_ASC("created_asc"),
	CREATED_DESC("created_desc"),
	ID_ASC("id_asc"),
	ID_DESC("id_desc"),
	VIEWS_ASC("views_asc"),
	VIEWS_DESC("views_desc");

	private final String code;

	PageSortOptionEnum(String code) {
		this.code = code;
	}

	public String getCode() {
		return code;
	}

	public boolean isDesc() {
		return this == CREATED_DESC || this == ID_DESC || this == VIEWS_DESC;
	}

	public static PageSortOptionEnum from(String raw) {
		if (raw == null || raw.isBlank()) {
			return CREATED_DESC;
		}

		String s = raw.trim().toLowerCase();

		// 1) 코드와 완전히 일치하는 케이스 우선
		return Arrays.stream(values())
			.filter(v -> v.code.equalsIgnoreCase(s))
			.findFirst()
			// 2) alias 지원 (created → created_desc 처럼)
			.orElseGet(() -> switch (s) {
				case "created" -> CREATED_DESC;
				case "id" -> ID_DESC;
				case "views" -> VIEWS_DESC;
				default -> CREATED_DESC;
			});
	}
}
