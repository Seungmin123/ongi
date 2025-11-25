package com.ongi.user.domain;

public class UserProfile {

	private Long id;

	private Long userId;

	private String displayName;

	// TODO List
	private String allergens;

	private Integer dietGoal;

	// TODO List
	private String dislikedIngredients;

	private UserProfile(
		Long userId,
		String displayName,
		String allergens,
		Integer dietGoal,
		String dislikedIngredients
	) {
		this.userId = userId;
		this.displayName = displayName;
		this.allergens = allergens;
		this.dietGoal = dietGoal;
		this.dislikedIngredients = dislikedIngredients;
	}

	public static UserProfile create(
		Long userId, String displayName, String allergens, Integer dietGoal, String dislikedIngredients
	) {
		return new UserProfile(userId, displayName, allergens, dietGoal, dislikedIngredients);
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public String getAllergens() {
		return allergens;
	}

	public void setAllergens(String allergens) {
		this.allergens = allergens;
	}

	public Integer getDietGoal() {
		return dietGoal;
	}

	public void setDietGoal(Integer dietGoal) {
		this.dietGoal = dietGoal;
	}

	public String getDislikedIngredients() {
		return dislikedIngredients;
	}

	public void setDislikedIngredients(String dislikedIngredients) {
		this.dislikedIngredients = dislikedIngredients;
	}
}
