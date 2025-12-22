package com.ongi.api.user.adapter.out.cache.persistence.projection;

public record MeBasicRow(
	String name,
	String birth,
	String zipCode,
	String address,
	String addressDetail
) {}