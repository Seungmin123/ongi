package com.ongi.api.ingredients.persistence;

import com.ongi.api.common.persistence.entity.BaseTimeEntity;
import com.ongi.ingredients.domain.enums.RecipeIngredientUnitEnum;
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
import org.hibernate.annotations.ColumnDefault;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "recipe_ingredient")
public class RecipeIngredientEntity extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "recipe_ingredient_id", nullable = false)
	private Long id;

	@Column(name = "recipe_id", nullable = false)
	private Long recipeId;

	@Column(name = "ingredient_id", nullable = false)
	private Long ingredientId;

	@ColumnDefault("1")
	@Column(name = "quantity", nullable = false)
	private Integer quantity;

	@Enumerated(EnumType.STRING)
	@Column(name = "unit", nullable = false, comment = "'g', 'ml', '큰술' 등")
	private RecipeIngredientUnitEnum unit;

	@Column(name = "note", nullable = false, comment = "'다진 것', '썰기' 등")
	private String note;

	@Column(name = "sort_order", nullable = false)
	private Integer sortOrder;

	@Builder
	public RecipeIngredientEntity(
		Long recipeId,
		Long ingredientId,
		Integer quantity,
		RecipeIngredientUnitEnum unit,
		String note,
		Integer sortOrder
	) {
		this.recipeId = recipeId;
		this.ingredientId = ingredientId;
		this.quantity = quantity;
		this.unit = unit;
		this.note = note;
		this.sortOrder = sortOrder;
	}

}
