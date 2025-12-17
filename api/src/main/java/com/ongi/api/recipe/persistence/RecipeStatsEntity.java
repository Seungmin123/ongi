package com.ongi.api.recipe.persistence;

import com.ongi.api.common.persistence.entity.BaseTimeEntity;
import com.ongi.recipe.domain.enums.RecipeCategoryEnum;
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
@Table(name = "recipe_stats")
public class RecipeStatsEntity extends BaseTimeEntity {

	@Id
	@Column(name = "recipe_id", nullable = false)
	private Long recipeId;

	@ColumnDefault("0")
	@Column(name = "like_count", nullable = false)
	private Long likeCount;

	@ColumnDefault("0")
	@Column(name = "comment_count", nullable = false)
	private Long commentCount;

	@ColumnDefault("0")
	@Column(name = "view_count", nullable = false)
	private Long viewCount;

	@Builder
	public RecipeStatsEntity(
		Long recipeId,
		Long likeCount,
		Long commentCount,
		Long viewCount
	) {
		this.recipeId = recipeId;
		this.likeCount = likeCount;
		this.commentCount = commentCount;
		this.viewCount = viewCount;
	}

}
