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
public class RecipeCategoryDailyMetricsId implements Serializable {

	@Column(name = "metric_date", nullable = false)
	private LocalDate metricDate;

	@Column(name = "category", nullable = false, length = 100)
	private String category;

	public RecipeCategoryDailyMetricsId(LocalDate metricDate, String category) {
		this.metricDate = metricDate;
		this.category = category;
	}
}
