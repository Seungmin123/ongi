package com.ongi.api.ingredients.persistence;

import com.ongi.api.common.persistence.entity.BaseTimeEntity;
import com.ongi.ingredients.domain.enums.NutritionEnum;
import com.ongi.ingredients.domain.enums.NutritionUnitEnum;
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
@Table(name = "nutrition")
public class NutritionEntity extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "nutrition_id", nullable = false)
	private Long id;

	@Enumerated(EnumType.STRING)
	@Column(name = "code", nullable = false, unique = true, length = 50)
	private NutritionEnum code;

	@Column(name = "display_name", nullable = false, length = 100)
	private String displayName;

	@Enumerated(EnumType.STRING)
	@Column(name = "unit", nullable = false, length = 20)
	private NutritionUnitEnum unit;

	@Builder
	public NutritionEntity(
		NutritionEnum code,
		NutritionUnitEnum unit
	) {
		this.code = code;
		this.displayName = code.getDisplayName();
		this.unit = unit;
	}

}
