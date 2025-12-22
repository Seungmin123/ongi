package com.ongi.api.user.adapter.out.cache.persistence.projection;

public record MePersonalizationRow(
	String allergens,
	Double dietGoal,
	String dislikedIngredients
) {}