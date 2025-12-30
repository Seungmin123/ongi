package com.ongi.api.user.web.dto;

import com.ongi.api.user.adapter.out.persistence.projection.AllergensGroupRow;
import com.ongi.api.user.adapter.out.persistence.projection.DislikedIngredientsRow;
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
		Double dietGoal,
		List<AllergensGroupRow> allergens,
		List<DislikedIngredientsRow> dislikedIngredients
	) {}
}
