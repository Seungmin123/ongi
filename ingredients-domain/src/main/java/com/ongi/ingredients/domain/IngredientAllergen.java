package com.ongi.ingredients.domain;

public class IngredientAllergen {

	private Long id;

	private Long ingredientId;

	private Long allergenGroupId;

	private Double confidence;

	private String reason;

	private IngredientAllergen(Long id, Long ingredientId, Long allergenGroupId, Double confidence, String reason) {
		this.id = id;
		this.ingredientId = ingredientId;
		this.allergenGroupId = allergenGroupId;
		this.confidence = confidence;
		this.reason = reason;
	}

	public static IngredientAllergen create(Long id, Long ingredientId, Long allergenGroupId, Double confidence, String reason) {
		return new IngredientAllergen(id, ingredientId, allergenGroupId, confidence, reason);
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getIngredientId() {
		return ingredientId;
	}

	public void setIngredientId(Long ingredientId) {
		this.ingredientId = ingredientId;
	}

	public Long getAllergenGroupId() {
		return allergenGroupId;
	}

	public void setAllergenGroupId(Long allergenGroupId) {
		this.allergenGroupId = allergenGroupId;
	}

	public Double getConfidence() {
		return confidence;
	}

	public void setConfidence(Double confidence) {
		this.confidence = confidence;
	}

	public String getReason() {
		return reason;
	}

	public void setReason(String reason) {
		this.reason = reason;
	}
}
