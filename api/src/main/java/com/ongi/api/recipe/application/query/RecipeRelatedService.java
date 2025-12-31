package com.ongi.api.recipe.application.query;

import com.ongi.api.ingredients.adapter.out.persistence.RecipeRelatedConfigEntity;
import com.ongi.api.ingredients.adapter.out.persistence.projection.RelatedRecipeRow;
import com.ongi.api.ingredients.adapter.out.persistence.repository.RecipeRelatedConfigRepository;
import com.ongi.api.ingredients.adapter.out.persistence.repository.RecipeRelatedNativeRepository;
import com.ongi.api.recipe.adapter.out.persistence.repository.RecipeCardQueryRepository;
import com.ongi.api.recipe.web.dto.RelatedRecipeItem;
import java.util.List;
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

	private final RecipeRelatedNativeRepository relatedRepo;

	private final RecipeCardQueryRepository cardRepo;

	private final RecipeRelatedConfigRepository configRepository;

	public List<RelatedRecipeItem> getRelated(Long recipeId, int limit) {
		var cfg = configRepository.findSingleton().orElseThrow();

		List<RelatedRecipeRow> candidates = relatedRepo.findRelatedByIdfNative(
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
		var cards = cardRepo.findCardsByIds(ids);

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
}
