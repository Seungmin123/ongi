package com.ongi.api.recipe.application.query;

import com.ongi.api.ingredients.adapter.out.persistence.RecipeRelatedConfigEntity;
import com.ongi.api.ingredients.adapter.out.persistence.projection.RelatedRecipeRow;
import com.ongi.api.ingredients.adapter.out.persistence.repository.RecipeRelatedConfigRepository;
import com.ongi.api.ingredients.adapter.out.persistence.repository.RecipeRelatedNativeRepository;
import com.ongi.api.recipe.adapter.out.persistence.metrics.projection.RecipeView7dRow;
import com.ongi.api.recipe.adapter.out.persistence.metrics.projection.RelatedRecipeFinalRow;
import com.ongi.api.recipe.adapter.out.persistence.metrics.repository.RecipeDailyMetricsNativeRepository;
import com.ongi.api.recipe.adapter.out.persistence.repository.RecipeCardQueryRepository;
import com.ongi.api.recipe.web.dto.RelatedRecipeItem;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional(transactionManager = "transactionManager", readOnly = true)
@Service
public class RecipeRelatedService {

	private final RecipeRelatedNativeRepository relatedRepository;

	private final RecipeCardQueryRepository cardRepository;

	private final RecipeRelatedConfigRepository configRepository;

	private final RecipeDailyMetricsNativeRepository dailyMetricsRepository;

	public List<RelatedRecipeItem> getRelated(Long recipeId, int limit) {
		var cfg = configRepository.findSingleton().orElseThrow();

		List<RelatedRecipeRow> candidates = relatedRepository.findRelatedByIdfNative(
			recipeId,
			limit,
			cfg.getIdfBase(),
			cfg.getRareMinIdf(),
			cfg.getCategoryAlpha(),
			cfg.getMinScore(),
			cfg.getMinOverlapCount(),
			cfg.getMinRareOverlapCount(),
			cfg.getMinCenteredScore()
		);

		Set<Long> ids = candidates.stream().map(RelatedRecipeRow::recipeId).collect(Collectors.toSet());
		var cards = cardRepository.findCardsByIds(ids);

		// score 붙여서 반환
		return candidates.stream()
			.map(row -> {
				var c = cards.get(row.recipeId());
				if (c == null) return null; // 혹시 레시피가 soft delete 됐다면 제외
				return new RelatedRecipeItem(
					c.recipeId(),
					c.title(),
					c.imageUrl(),
					c.cookingTimeMin(),
					c.difficulty(),
					c.category(),
					row.score()
				);
			})
			.filter(Objects::nonNull)
			.toList();
	}

	@Transactional(readOnly = true, transactionManager = "transactionManager")
	public List<RelatedRecipeFinalRow> findRelatedWithPopularityBoost(Long recipeId, int limit) {
		var cfg = configRepository.findSingleton().orElseThrow();

		List<RelatedRecipeRow> related = relatedRepository.findRelatedByIdfNative(
			recipeId,
			limit,
			cfg.getIdfBase(),
			cfg.getRareMinIdf(),
			cfg.getCategoryAlpha(),
			cfg.getMinScore(),
			cfg.getMinOverlapCount(),
			cfg.getMinRareOverlapCount(),
			cfg.getMinCenteredScore()
		);
		if (related.isEmpty()) return List.of();

		// 2) view_7d 집계(IN)
		List<Long> ids = related.stream().map(RelatedRecipeRow::recipeId).distinct().toList();
		List<RecipeView7dRow> views = dailyMetricsRepository.findView7dByRecipeIds(ids);

		Map<Long, Long> view7dMap = new HashMap<>();
		for (RecipeView7dRow v : views) {
			view7dMap.put(v.recipeId(), v.view7d());
		}

		// 3) final_score 계산 + 정렬 + limit
		return related.stream()
			.map(r -> {
				long v = view7dMap.getOrDefault(r.recipeId(), 0L);
				double finalScore = r.score() * Math.log(1.0 + v); // 자연로그
				return new RelatedRecipeFinalRow(r.recipeId(), r.score(), v, finalScore);
			})
			.sorted(Comparator
				.comparingDouble(RelatedRecipeFinalRow::finalScore).reversed()
				.thenComparing(RelatedRecipeFinalRow::recipeId, Comparator.reverseOrder())
			)
			.limit(limit)
			.toList();
	}
}
