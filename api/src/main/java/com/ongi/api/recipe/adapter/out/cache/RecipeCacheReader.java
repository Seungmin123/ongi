package com.ongi.api.recipe.adapter.out.cache;

import com.ongi.api.ingredients.adapter.out.persistence.IngredientAdapter;
import com.ongi.api.recipe.adapter.out.persistence.RecipeAdapter;
import com.ongi.api.recipe.adapter.out.persistence.RecipeDetailMapper;
import com.ongi.api.recipe.web.dto.RecipeCacheValue;
import com.ongi.api.recipe.web.dto.RecipeIngredientCacheValue;
import com.ongi.api.recipe.web.dto.RecipeStepsCacheValue;
import com.ongi.recipe.domain.Recipe;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class RecipeCacheReader {

	private final RecipeAdapter recipeAdapter;

	private final IngredientAdapter ingredientAdapter;

	private final RecipeCategoryCacheStore recipeCategoryCacheStore;

	@Cacheable(
		cacheNames = "recipeDetail",
		key = "'recipe:' + #recipeId + ':detail:' + #ver",
		unless = "#result == null"
	)
	@Transactional(
		readOnly = true,
		transactionManager = "transactionManager"
	)
	public RecipeCacheValue getRecipeById(Long recipeId, int ver) {
		Recipe r = recipeAdapter.findRecipeById(recipeId).orElseThrow(() -> new IllegalStateException("Recipe not found"));
		// User Action Consumer Cache Hit 위하여 여기서 삽입
		recipeCategoryCacheStore.putIfAbsent(recipeId, r.getCategory().getCode());
		return RecipeCacheValue.from(r);
	}

	@Cacheable(
		cacheNames = "recipeIngredient",
		key = "'recipe:' + #recipeId + ':ingredients:' + #ver"
	)
	@Transactional(
		readOnly = true,
		transactionManager = "transactionManager"
	)
	public RecipeIngredientCacheValue getRecipeIngredients(Long recipeId, int ver) {
		var list = ingredientAdapter.findRecipeIngredientByRecipeId(recipeId).stream()
			.map(RecipeDetailMapper::toIngredientResponse)
			.toList();
		return new RecipeIngredientCacheValue(list);
	}

	@Cacheable(
		cacheNames = "recipeSteps",
		key = "'recipe:' + #recipeId + ':steps:' + #ver"
	)
	@Transactional(
		readOnly = true,
		transactionManager = "transactionManager"
	)
	public RecipeStepsCacheValue getRecipeSteps(Long recipeId, int ver) {
		var list = recipeAdapter.findRecipeStepsByRecipeId(recipeId).stream()
			.map(RecipeDetailMapper::toStepsResponse)
			.toList();
		return new RecipeStepsCacheValue(list);
	}
}
