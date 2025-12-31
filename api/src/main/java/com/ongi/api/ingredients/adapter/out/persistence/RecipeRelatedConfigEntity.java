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

	@Column(name = "rare_min_idf", nullable = false, comment = "p50")
	private Double rareMinIdf;

	@Column(name = "category_alpha", nullable = false, comment = "카테고리 겹칠 경우 가산점 / 0.5 ~ 2.0")
	private Double categoryAlpha;

	@Column(name = "min_score", nullable = false, comment = "스코어 하한")
	private Double minScore;

	@Column(name = "min_overlap_count", nullable = false, comment = "겹치는 재료 최소 수")
	private Integer minOverlapCount;

	@Column(name = "min_rare_overlap_count", nullable = false, comment = "흔한 재료만 겹치면 컷 / 희귀 재료 최소 수")
	private Integer minRareOverlapCount;

	@ColumnDefault("0")
	@Column(name = "min_centered_score", nullable = false, comment = "centered_sum_idf 기준 하한")
	private Double minCenteredScore;

	@Column(name = "pop_beta", nullable = false, comment = "view 데이터가 없을 경우 대비한 score 가산점 / 0.3 ~ 2.0")
	private Double popBeta;

	@Column(name = "updated_at", nullable = false)
	private LocalDateTime updatedAt;

}
