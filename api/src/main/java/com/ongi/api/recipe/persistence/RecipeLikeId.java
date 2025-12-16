package com.ongi.api.recipe.persistence;

import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class RecipeLikeId implements Serializable {

	private Long recipeId;
	private Long userId;

	protected RecipeLikeId() {
	}

	public RecipeLikeId(Long recipeId, Long userId) {
		this.recipeId = recipeId;
		this.userId = userId;
	}

	public Long getRecipeId() {
		return recipeId;
	}

	public Long getUserId() {
		return userId;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof RecipeLikeId)) return false;
		RecipeLikeId that = (RecipeLikeId) o;
		return Objects.equals(recipeId, that.recipeId)
			&& Objects.equals(userId, that.userId);
	}

	@Override
	public int hashCode() {
		return Objects.hash(recipeId, userId);
	}
}
