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
public class RecipeDailyMetricsId implements Serializable {

	@Column(name = "metric_date", nullable = false)
	private LocalDate metricDate;

	@Column(name = "recipe_id", nullable = false)
	private Long recipeId;

	public RecipeDailyMetricsId(LocalDate metricDate, Long recipeId) {
		this.metricDate = metricDate;
		this.recipeId = recipeId;
	}
}
