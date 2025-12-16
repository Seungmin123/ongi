package com.ongi.api.user.persistence.projection;

public record MePersonalizationRow(
	String allergens,
	Double dietGoal,
	String dislikedIngredients
) {}