package com.ongi.api.user.web.dto;

import java.util.List;

public record MyPageResponse(
	Summary summary,
	Basic basic,
	Personalization personalization
) {

	public record Summary(
		String email,
		String displayName,
		String profileImageUrl
	) {}

	public record Basic(
		String name,
		String birth,
		String zipCode,
		String address,
		String addressDetail
	) {}

	public record Personalization(
		List<String> allergens,
		Double dietGoal,
		List<String> dislikedIngredients
	) {}
}
