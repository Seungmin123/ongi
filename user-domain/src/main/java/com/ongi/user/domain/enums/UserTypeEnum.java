package com.ongi.user.domain.enums;

public enum UserTypeEnum {

	EMAIL("EMAIL"),
	APPLE("APPLE"),
	NAVER("NAVER"),
	KAKAO("KAKAO"),
	GOOGLE("GOOGLE");

	private final String code;

	UserTypeEnum(String code) {
		this.code = code;
	}

	public String getCode() {
		return code;
	}
}
