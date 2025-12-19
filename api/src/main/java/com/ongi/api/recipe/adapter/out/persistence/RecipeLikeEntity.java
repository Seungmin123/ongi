package com.ongi.api.recipe.adapter.out.persistence;

import com.ongi.api.common.persistence.entity.BaseTimeEntity;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(
	name = "recipe_like",
	indexes = {
		@Index(
			name = "idx_recipe_like_user_id",
			columnList = "user_id"
		),
		@Index(
			name = "idx_recipe_like_recipe_id",
			columnList = "recipe_id"
		)
	})
public class RecipeLikeEntity extends BaseTimeEntity {

	@EmbeddedId
	private RecipeLikeId id;

	@Builder
	public RecipeLikeEntity(Long userId, Long recipeId) {
		this.id = new RecipeLikeId(userId, recipeId);
	}

	public Long getRecipeId() {
		return id.getRecipeId();
	}

	public Long getUserId() {
		return id.getUserId();
	}
}
