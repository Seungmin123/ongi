package com.ongi.recipe.domain;

public class RecipeSteps {

	private Long id;

	private Long recipeId;

	private Integer stepOrder;

	private String title;

	private String description;

	private Integer estimatedMin;

	private Integer waitMin;

	private String temperature;

	private String imageUrl;

	private String videoUrl;

	private RecipeSteps(
		Long recipeId,
		Integer stepOrder,
		String title,
		String description,
		Integer estimatedMin,
		Integer waitMin,
		String temperature,
		String imageUrl,
		String videoUrl
	) {
		this.recipeId = recipeId;
		this.stepOrder = stepOrder;
		this.title = title;
		this.description = description;
		this.estimatedMin = estimatedMin;
		this.waitMin = waitMin;
		this.temperature = temperature;
		this.imageUrl = imageUrl;
		this.videoUrl = videoUrl;
	}

	public static RecipeSteps create(
		Long recipeId, Integer stepOrder, String title, String description, Integer estimatedMin, Integer waitMin,
		String temperature, String imageUrl, String videoUrl
	) {
		return new RecipeSteps(recipeId, stepOrder, title, description, estimatedMin, waitMin, temperature, imageUrl, videoUrl);
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getRecipeId() {
		return recipeId;
	}

	public void setRecipeId(Long recipeId) {
		this.recipeId = recipeId;
	}

	public Integer getStepOrder() {
		return stepOrder;
	}

	public void setStepOrder(Integer stepOrder) {
		this.stepOrder = stepOrder;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Integer getEstimatedMin() {
		return estimatedMin;
	}

	public void setEstimatedMin(Integer estimatedMin) {
		this.estimatedMin = estimatedMin;
	}

	public Integer getWaitMin() {
		return waitMin;
	}

	public void setWaitMin(Integer waitMin) {
		this.waitMin = waitMin;
	}

	public String getTemperature() {
		return temperature;
	}

	public void setTemperature(String temperature) {
		this.temperature = temperature;
	}

	public String getImageUrl() {
		return imageUrl;
	}

	public void setImageUrl(String imageUrl) {
		this.imageUrl = imageUrl;
	}

	public String getVideoUrl() {
		return videoUrl;
	}

	public void setVideoUrl(String videoUrl) {
		this.videoUrl = videoUrl;
	}
}
