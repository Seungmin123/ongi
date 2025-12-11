package com.ongi.recipe.domain;

import com.ongi.recipe.domain.enums.RecipeCategoryEnum;
import com.ongi.recipe.domain.enums.RecipeDifficultyEnum;

public class Recipe {

	private Long id;

	private Long authorId;

	private String title;

	private String description;

	private Double serving;

	private Integer cookingTimeMin;

	private RecipeDifficultyEnum difficulty;

	private String imageUrl;

	private String videoUrl;

	private String source;

	private RecipeCategoryEnum category;

	// TODO likes, comments 추가?

	private Recipe(
		Long id,
		Long authorId,
		String title,
		String description,
		Double serving,
		Integer cookingTimeMin,
		RecipeDifficultyEnum difficulty,
		String imageUrl,
		String videoUrl,
		String source,
		RecipeCategoryEnum category
	) {
		this.id = id;
		this.authorId = authorId;
		this.title = title;
		this.description = description;
		this.serving = serving;
		this.cookingTimeMin = cookingTimeMin;
		this.difficulty = difficulty;
		this.imageUrl = imageUrl;
		this.videoUrl = videoUrl;
		this.source = source;
		this.category = category;
	}

	public static Recipe create(
		Long id, Long authorId, String title, String description, Double serving, Integer cookingTimeMin,
		RecipeDifficultyEnum difficulty, String imageUrl, String videoUrl, String source, RecipeCategoryEnum category
	) {
		return new Recipe(id, authorId, title, description, serving, cookingTimeMin, difficulty, imageUrl, videoUrl, source, category);
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getAuthorId() {
		return authorId;
	}

	public void setAuthorId(Long authorId) {
		this.authorId = authorId;
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

	public Double getServing() {
		return serving;
	}

	public void setServing(Double serving) {
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

	public RecipeCategoryEnum getCategory() {
		return category;
	}

	public void setCategory(RecipeCategoryEnum category) {
		this.category = category;
	}
}
