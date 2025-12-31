package com.ongi.api.ingredients.adapter.out.persistence.projection;

public record RelatedRecipeRow(
	long recipeId,
	double rawSumIdf,   // 순수 idf 합
	int overlapCnt,     // 겹친 재료 개수
	int rareOverlapCnt, // 희귀 재료(>= rareMinIdf) 겹친 개수
	double score        // 최종 점수 (rawSumIdf + categoryBoost)double score,       // 최종 점수 (rawSumIdf + categoryBoost)
) {

}
