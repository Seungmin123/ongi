package com.ongi.api.recipe.persistence;

import com.ongi.api.common.persistence.entity.BaseTimeEntity;
import com.ongi.recipe.domain.enums.RecipeDifficultyEnum;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "recipe")
public class RecipeEntity extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "recipe_id", nullable = false)
	private Long id;

	@Column(name = "title", nullable = false)
	private String title;

	@Column(name = "description")
	private String description;

	@ColumnDefault("1")
	@Column(name = "serving", nullable = false)
	private Double serving;

	@ColumnDefault("0")
	@Column(name = "cooking_time_min", nullable = false)
	private Integer cookingTimeMin;

	@Enumerated(EnumType.STRING)
	@Column(name = "difficulty", nullable = false)
	private RecipeDifficultyEnum difficulty;

	@Column(name = "image_url")
	private String imageUrl;

	@Column(name = "video_url")
	private String videoUrl;

	@Column(name = "source", comment = "출처")
	private String source;

	@Column(name = "category")
	private String category;

	@Builder
	public RecipeEntity(
		Long id,
		String title,
		String description,
		Double serving,
		Integer cookingTimeMin,
		RecipeDifficultyEnum difficulty,
		String imageUrl,
		String videoUrl,
		String source,
		String category
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
		this.category = category;
	}

}
