package com.ongi.api.user.persistence.projection;

public record MeSummaryRow(
	String email,
	String displayName,
	String profileImageUrl
) {}