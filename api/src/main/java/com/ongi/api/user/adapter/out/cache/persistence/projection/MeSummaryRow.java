package com.ongi.api.user.adapter.out.cache.persistence.projection;

public record MeSummaryRow(
	String email,
	String displayName,
	String profileImageUrl
) {}