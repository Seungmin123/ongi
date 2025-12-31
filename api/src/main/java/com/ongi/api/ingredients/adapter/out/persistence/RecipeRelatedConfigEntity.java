package com.ongi.api.ingredients.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "recipe_related_config")
public class RecipeRelatedConfigEntity {

	@Id
	@GeneratedValue(strategy= GenerationType.IDENTITY)
	@Column(name = "config_id", nullable = false)
	private Long id;

	@Column(name = "idf_base", nullable = false, comment = "p25")
	private Double idfBase;

	@Column(name = "category_alpha", nullable = false, comment = "카테고리 겹칠 경우 가산점 / 0.5 ~ 2.0")
	private Double categoryAlpha;

	@Column(name = "min_score", nullable = false, comment = "스코어 하한")
	private Double minScore;

	@Column(name = "min_overlap_count", nullable = false, comment = "겹치는 재료 최소 수")
	private Integer minOverlapCount;

	@ColumnDefault("0")
	@Column(name = "min_centered_score", nullable = false, comment = "희귀 재료들 idf 초과분 합")
	private Double minCenteredScore;

	@Column(name = "seed_k", nullable = false, comment = "연관 판단용 seed 재료 개수(기준레시피 idf 상위 K개) / 2~4")
	private Integer seedK;

	@Column(name = "seed_min_hit", nullable = false, comment = "후보 레시피가 포함해야 하는 seed 재료 최소 개수. 1이면 coverage↑, 2이면 precision↑")
	private Integer seedMinHit;

	@Column(name = "seed_min_idf", nullable = false, comment = "seed 후보 재료의 최소 idf 하한. 0이면 미사용. 추천: idf_base(p25) 또는 0")
	private Double seedMinIdf;

	@Column(name = "seed_alpha", nullable = false, comment = "seed 1개 겹칠 때마다 score에 더하는 가산점. centered_sum_idf 스케일(대략 0~2) 기준으로 0.1~0.5")
	private Double seedAlpha;

	@Column(name = "pop_beta", nullable = false, comment = "popularity(LOG(1+view_7d)) 가산항 가중치. view 없는 상태 대비 안정화용. 추천 0.1~0.6")
	private Double popBeta;

	@Column(name = "updated_at", nullable = false)
	private LocalDateTime updatedAt;

}
