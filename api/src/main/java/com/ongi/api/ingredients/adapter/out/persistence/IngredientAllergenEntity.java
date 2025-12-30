package com.ongi.api.ingredients.adapter.out.persistence;

import com.ongi.api.common.persistence.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name="ingredient_allergen",
	uniqueConstraints = @UniqueConstraint(
		name="uk_ing_allergen", columnNames={"ingredient_id","allergen_group_id"}),
	indexes = {
		@Index(name="idx_ing_allergen_allergen", columnList="allergen_group_id, ingredient_id"),
		@Index(name="idx_ing_allergen_ing", columnList="ingredient_id, allergen_group_id")
	}
)
public class IngredientAllergenEntity extends BaseTimeEntity {
	@Id @GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(name="ingredient_allergen_id")
	private Long id;

	@Column(name="ingredient_id", nullable=false)
	private Long ingredientId;

	@Column(name="allergen_group_id", nullable=false)
	private Long allergenGroupId;

	@ColumnDefault("0")
	@Column(name="confidence")
	private Double confidence;

	@Column(name="reason")
	private String reason;

	@Builder
	public IngredientAllergenEntity(Long id, Long ingredientId, Long allergenGroupId, Double confidence, String reason) {
		this.id = id;
		this.ingredientId = ingredientId;
		this.allergenGroupId = allergenGroupId;
		this.confidence = confidence;
		this.reason = reason;
	}
}
