package com.ongi.user.domain;

public class UserAllergen {

	private Long id;

	private Long userId;

	private Long allergenGroupId;

	private UserAllergen(Long id, Long userId, Long allergenGroupId) {
		this.id = id;
		this.userId = userId;
		this.allergenGroupId = allergenGroupId;
	}

	public static UserAllergen create(Long id, Long userId, Long allergenGroupId) {
		return new UserAllergen(id, userId, allergenGroupId);
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

	public Long getAllergenGroupId() {
		return allergenGroupId;
	}

	public void setAllergenGroupId(Long allergenGroupId) {
		this.allergenGroupId = allergenGroupId;
	}
}
