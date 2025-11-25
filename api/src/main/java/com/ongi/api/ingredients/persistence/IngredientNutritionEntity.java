package com.ongi.api.ingredients.persistence;

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
@Table(name = "ingredient_nutrition")
public class IngredientNutritionEntity extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ingredient_nutrition_id", nullable = false)
	private Long id;

	@Column(name = "ingredient_id", nullable = false)
	private Long ingredientId;

	@Column(name = "nutrition_id", nullable = false)
	private Long nutritionId;

	@Builder
	public IngredientNutritionEntity(
		Long ingredientId,
		Long nutritionId
	) {
		this.ingredientId = ingredientId;
		this.nutritionId = nutritionId;
	}

}
