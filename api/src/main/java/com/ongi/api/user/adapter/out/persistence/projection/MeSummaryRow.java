package com.ongi.api.user.adapter.out.persistence.projection;

public record MeSummaryRow(
	String email,
	String displayName,
	String profileImageUrl
) {}