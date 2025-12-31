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
public class RecipeCategoryWindowMetricsId implements Serializable {

	@Column(name = "as_of_date", nullable = false)
	private LocalDate asOfDate;

	@Column(name = "window_days", nullable = false)
	private Integer windowDays;

	@Column(name = "category", nullable = false, length = 100)
	private String category;

	public RecipeCategoryWindowMetricsId(LocalDate asOfDate, Integer windowDays, String category) {
		this.asOfDate = asOfDate;
		this.windowDays = windowDays;
		this.category = category;
	}
}
