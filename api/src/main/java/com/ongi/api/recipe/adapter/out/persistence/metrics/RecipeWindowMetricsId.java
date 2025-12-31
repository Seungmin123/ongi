package com.ongi.api.recipe.adapter.out.persistence.metrics;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.time.LocalDate;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@EqualsAndHashCode
@Embeddable
public class RecipeWindowMetricsId implements Serializable {

	@Column(name = "as_of_date", nullable = false)
	private LocalDate asOfDate;

	@Column(name = "window_days", nullable = false)
	private Integer windowDays;

	@Column(name = "recipe_id", nullable = false)
	private Long recipeId;

	public RecipeWindowMetricsId(LocalDate asOfDate, Integer windowDays, Long recipeId) {
		this.asOfDate = asOfDate;
		this.windowDays = windowDays;
		this.recipeId = recipeId;
	}
}
