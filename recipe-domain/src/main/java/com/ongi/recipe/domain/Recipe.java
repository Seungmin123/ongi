package com.ongi.recipe.domain;

import com.ongi.recipe.domain.enums.RecipeDifficultyEnum;

public class Recipe {

	private Long id;

	private String title;

	private String description;

	private Integer serving;

	private Integer cookingTimeMin;

	private RecipeDifficultyEnum difficulty;

	private String imageUrl;

	private String videoUrl;

	private String source;

	private Recipe(
		Long id,
		String title,
		String description,
		Integer serving,
		Integer cookingTimeMin,
		RecipeDifficultyEnum difficulty,
		String imageUrl,
		String videoUrl,
		String source
	) {
		this.id = id;
		this.title = title;
		this.description = description;
		this.serving = serving;
		this.cookingTimeMin = cookingTimeMin;
		this.difficulty = difficulty;
		this.imageUrl = imageUrl;
		this.videoUrl = videoUrl;
		this.source = source;
	}

	public static Recipe create(
		Long id, String title, String description, Integer serving, Integer cookingTimeMin,
		RecipeDifficultyEnum difficulty, String imageUrl, String videoUrl, String source
	) {
		return new Recipe(id, title, description, serving, cookingTimeMin, difficulty, imageUrl, videoUrl, source);
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
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

	public Integer getServing() {
		return serving;
	}

	public void setServing(Integer serving) {
		this.serving = serving;
	}

	public Integer getCookingTimeMin() {
		return cookingTimeMin;
	}

	public void setCookingTimeMin(Integer cookingTimeMin) {
		this.cookingTimeMin = cookingTimeMin;
	}

	public RecipeDifficultyEnum getDifficulty() {
		return difficulty;
	}

	public void setDifficulty(RecipeDifficultyEnum difficulty) {
		this.difficulty = difficulty;
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

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}
}
