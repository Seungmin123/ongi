package com.ongi.api.recipe.adapter.out.persistence.metrics;

import com.ongi.api.common.persistence.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "recipe_window_metrics")
public class RecipeWindowMetricsEntity extends BaseTimeEntity {

	@EmbeddedId
	private RecipeWindowMetricsId id;

	@Column(name = "view_cnt", nullable = false)
	private long viewCnt;

	@Column(name = "engaged_view_cnt", nullable = false)
	private long engagedViewCnt;

	@Column(name = "like_net", nullable = false)
	private long likeNet;

	@Column(name = "save_net", nullable = false)
	private long saveNet;

	@Column(name = "dwell_ms_sum", nullable = false)
	private long dwellMsSum;

	@Column(name = "scroll_depth_sum", nullable = false)
	private long scrollDepthSum;

}
