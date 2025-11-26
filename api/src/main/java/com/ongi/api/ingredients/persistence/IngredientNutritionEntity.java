package com.ongi.api.ingredients.persistence;

import com.ongi.api.common.persistence.entity.BaseTimeEntity;
import com.ongi.ingredients.domain.enums.NutritionBasisEnum;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(
	name = "ingredient_nutrition",
	uniqueConstraints = @UniqueConstraint(
		columnNames = {"ingredient_id", "nutrition_id"}
	)
)
public class IngredientNutritionEntity extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ingredient_nutrition_id", nullable = false)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "ingredient_id", nullable = false)
	private IngredientEntity ingredient;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "nutrition_id", nullable = false)
	private NutritionEntity nutrition;

	@Column(name = "quantity", nullable = false)
	private Double quantity;

	@Column(name = "basis", nullable = false, comment = "'100g', '100ml', '조각', '인분' 등 기준")
	private NutritionBasisEnum basis;

	@Builder
	public IngredientNutritionEntity(
		IngredientEntity ingredient,
		NutritionEntity nutrition,
		Double quantity,
		NutritionBasisEnum basis
	) {
		this.ingredient = ingredient;
		this.nutrition = nutrition;
		this.quantity = quantity;
		this.basis = basis;
	}

}
