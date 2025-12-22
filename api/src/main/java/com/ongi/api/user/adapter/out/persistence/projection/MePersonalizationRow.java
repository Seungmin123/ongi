package com.ongi.api.user.adapter.out.persistence.projection;

public record MePersonalizationRow(
	String allergens,
	Double dietGoal,
	String dislikedIngredients
) {}