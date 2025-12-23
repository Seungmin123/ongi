package com.ongi.api.community.adatper.out.persistence.enums;

import java.util.Arrays;

public enum CommentSortOption {

	CREATED_ASC("created_asc"),
	CREATED_DESC("created_desc");

	private final String code;

	CommentSortOption(String code) {
		this.code = code;
	}

	public String getCode() {
		return code;
	}

	public static CommentSortOption from(String raw) {
		if (raw == null || raw.isBlank()) {
			return CREATED_ASC;
		}

		String s = raw.trim().toLowerCase();

		// 1) 코드와 완전히 일치하는 케이스 우선
		return Arrays.stream(values())
			.filter(v -> v.code.equalsIgnoreCase(s))
			.findFirst()
			.orElseGet(() -> switch (s) {
				case "created" -> CREATED_ASC;
				default -> CREATED_DESC;
			});
	}
}
