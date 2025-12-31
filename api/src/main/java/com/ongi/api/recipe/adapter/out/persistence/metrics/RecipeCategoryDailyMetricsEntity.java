package com.ongi.api.recipe.adapter.out.persistence.metrics;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "recipe_category_daily_metrics")
public class RecipeCategoryDailyMetricsEntity {

	@EmbeddedId
	private RecipeCategoryDailyMetricsId id;

	@Column(name = "view_cnt", nullable = false)
	private long viewCnt;

	@Column(name = "engaged_view_cnt", nullable = false)
	private long engagedViewCnt;

	@Column(name = "like_net", nullable = false)
	private long likeNet;

	@Column(name = "save_net", nullable = false)
	private long saveNet;

	@Column(name = "created_at", nullable = false)
	private LocalDateTime createdAt;

	@Column(name = "updated_at", nullable = false)
	private LocalDateTime updatedAt;
}
