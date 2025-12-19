package com.ongi.api.recipe.adapter.out.persistence;

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

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "recipe_tags")
public class RecipeTagsEntity extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "recipe_tags_id", nullable = false)
	private Long id;

	@Column(name = "recipe_id", nullable = false)
	private Long recipeId;

	@Column(name = "tag", nullable = false)
	private String tag;

	@Builder
	public RecipeTagsEntity(
		Long id,
		Long recipeId,
		String tag
	) {
		this.id = id;
		this.recipeId = recipeId;
		this.tag = tag;
	}

}
