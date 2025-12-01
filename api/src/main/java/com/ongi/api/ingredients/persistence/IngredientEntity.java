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

	@Column(name = "code")
	private String code;

	@Enumerated(EnumType.STRING)
	@Column(name = "category", nullable = false)
	private IngredientCategoryEnum category;

	@Column(name = "calories_kcal", nullable = false)
	private Double caloriesKcal;

	@Column(name = "protein_g", nullable = false)
	private Double proteinG;

	@Column(name = "fat_g", nullable = false)
	private Double fatG;

	@Column(name = "carbs_g", nullable = false)
	private Double carbsG;

	@Builder
	public IngredientEntity(
		Long id,
		String name,
		String code,
		IngredientCategoryEnum category,
		Double caloriesKcal,
		Double proteinG,
		Double fatG,
		Double carbsG
	) {
		this.id = id;
		this.name = name;
		this.code = code;
		this.category = category;
		this.caloriesKcal = caloriesKcal;
		this.proteinG = proteinG;
		this.fatG = fatG;
		this.carbsG = carbsG;
	}

}
