package com.ongi.api.recipe.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class RecipeLikeId implements Serializable {

	@Column(name = "user_id", nullable = false)
	private Long userId;

	@Column(name = "recipe_id", nullable = false)
	private Long recipeId;

	protected RecipeLikeId() {
	}

	public RecipeLikeId(Long userId, Long recipeId) {
		this.userId = userId;
		this.recipeId = recipeId;
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
		return Objects.equals(userId, that.userId)
			&& Objects.equals(recipeId, that.recipeId);
	}

	@Override
	public int hashCode() {
		return Objects.hash(userId, recipeId);
	}
}
