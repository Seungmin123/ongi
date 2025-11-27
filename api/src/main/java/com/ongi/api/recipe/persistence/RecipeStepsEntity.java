package com.ongi.api.recipe.persistence;

import com.ongi.api.common.persistence.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "recipe_steps")
public class RecipeStepsEntity extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "recipe_steps_id", nullable = false)
	private Long id;

	@Column(name = "recipe_id", nullable = false)
	private Long recipeId;

	@ColumnDefault("1")
	@Column(name = "step_order", nullable = false)
	private Integer stepOrder;

	@Column(name = "title", nullable = false)
	private String title;

	@Column(name = "description", nullable = false)
	private String description;

	@Column(name = "estimated_min", nullable = false, comment = "예상 소요 시간(분)")
	private Integer estimatedMin;

	@Column(name = "wait_min", comment = "숙성/끓이기 등 기다리는 시간(분)")
	private Integer waitMin;

	@Column(name = "temperature", comment = "오븐 온도 또는 불 세기")
	private String temperature;

	@Column(name = "image_url")
	private String imageUrl;

	@Column(name = "video_url")
	private String videoUrl;

	@Builder
	public RecipeStepsEntity(
		Long id,
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
		this.id = id;
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

	// TODO 수정 관련 기능 추가

}
