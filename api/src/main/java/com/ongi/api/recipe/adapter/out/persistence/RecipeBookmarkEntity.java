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
	name = "recipe_bookmark",
	indexes = {
		@Index(
			name = "idx_recipe_bookmark_user_id",
			columnList = "user_id"
		),
		@Index(
			name = "idx_recipe_bookmark_recipe_id",
			columnList = "recipe_id"
		)
	})
public class RecipeBookmarkEntity extends BaseTimeEntity {

	@EmbeddedId
	private RecipeBookmarkId id;

	@Builder
	public RecipeBookmarkEntity(Long userId, Long recipeId) {
		this.id = new RecipeBookmarkId(userId, recipeId);
	}

	public Long getRecipeId() {
		return id.getRecipeId();
	}

	public Long getUserId() {
		return id.getUserId();
	}
}
