package com.ongi.api.ingredients.persistence;

import com.ongi.api.common.persistence.entity.BaseTimeEntity;
import com.ongi.ingredients.domain.enums.IngredientCategoryEnum;
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

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "ingredient")
public class IngredientEntity extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ingredient_id", nullable = false)
	private Long id;

	@Column(name = "name", nullable = false)
	private String name;

	@Enumerated(EnumType.STRING)
	@Column(name = "category", nullable = false)
	private IngredientCategoryEnum category;

	@Builder
	public IngredientEntity(
		String name,
		IngredientCategoryEnum category
	) {
		this.name = name;
		this.category = category;
	}

}
