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

	// Mark 2 / 식재료 간 IDF + 인기도 집계
	@Transactional(readOnly = true, transactionManager = "transactionManager")
	public List<RelatedRecipeItem> findRelatedWithPopularityBoost(Long recipeId, int limit) {
		var cfg = configRepository.findSingleton().orElseThrow();

		List<RelatedRecipeRow> related = relatedRepository.findRelatedWithPopularityBoost(
			recipeId,
			limit,
			cfg.getCategoryAlpha(),
			cfg.getIdfBase(),
			cfg.getMinScore(),
			cfg.getMinOverlapCount(),
			cfg.getMinCenteredScore(),
			cfg.getSeedK(),
			cfg.getSeedMinIdf(),
			cfg.getSeedMinHit(),
			cfg.getSeedAlpha(),
			cfg.getPopBeta()
		);
		if (related.isEmpty()) return List.of();

		// 2) view_7d 집계(IN)
		Set<Long> ids = related.stream().map(RelatedRecipeRow::recipeId).distinct().collect(Collectors.toSet());
		var cards = cardRepository.findCardsByIds(ids);

		// score 붙여서 반환
		return related.stream()
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
}
