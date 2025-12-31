package com.ongi.api.recipe.adapter.out.persistence.metrics;

import com.ongi.api.common.persistence.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicUpdate;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "recipe_daily_metrics")
@DynamicUpdate
public class RecipeDailyMetricsEntity extends BaseTimeEntity {

	@EmbeddedId
	private RecipeDailyMetricsId id;

	@Column(name = "view_cnt", nullable = false)
	private long viewCnt;

	@Column(name = "engaged_view_cnt", nullable = false)
	private long engagedViewCnt;

	@Column(name = "like_cnt", nullable = false)
	private long likeCnt;

	@Column(name = "unlike_cnt", nullable = false)
	private long unlikeCnt;

	@Column(name = "save_cnt", nullable = false)
	private long saveCnt;

	@Column(name = "unsave_cnt", nullable = false)
	private long unsaveCnt;

	@Column(name = "dwell_ms_sum", nullable = false)
	private long dwellMsSum;

	@Column(name = "dwell_ms_max", nullable = false)
	private int dwellMsMax;

	@Column(name = "scroll_depth_sum", nullable = false)
	private long scrollDepthSum;

	@Column(name = "scroll_depth_max", nullable = false)
	private short scrollDepthMax; // tinyint unsigned

	@Column(name = "dwell_ge_3s_cnt", nullable = false)
	private long dwellGe3sCnt;

	@Column(name = "dwell_ge_10s_cnt", nullable = false)
	private long dwellGe10sCnt;

	@Column(name = "scroll_ge_50_cnt", nullable = false)
	private long scrollGe50Cnt;

	@Column(name = "scroll_ge_90_cnt", nullable = false)
	private long scrollGe90Cnt;

}
