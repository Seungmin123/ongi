package com.ongi.api.recipe.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;
import lombok.Getter;

@Getter
@Embeddable
public class RecipeBookmarkId implements Serializable {

	@Column(name = "user_id", nullable = false)
	private Long userId;

	@Column(name = "recipe_id", nullable = false)
	private Long recipeId;

	protected RecipeBookmarkId() {
	}

	public RecipeBookmarkId(Long userId, Long recipeId) {
		this.userId = userId;
		this.recipeId = recipeId;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof RecipeBookmarkId)) return false;
		RecipeBookmarkId that = (RecipeBookmarkId) o;
		return Objects.equals(userId, that.userId)
			&& Objects.equals(recipeId, that.recipeId);
	}

	@Override
	public int hashCode() {
		return Objects.hash(userId, recipeId);
	}
}
