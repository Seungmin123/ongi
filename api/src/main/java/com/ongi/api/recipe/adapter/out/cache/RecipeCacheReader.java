package com.ongi.api.recipe.adapter.out.cache;

import com.ongi.api.ingredients.persistence.IngredientAdapter;
import com.ongi.api.recipe.adapter.out.persistence.RecipeAdapter;
import com.ongi.api.recipe.adapter.out.persistence.RecipeDetailMapper;
import com.ongi.api.recipe.web.dto.RecipeIngredientResponse;
import com.ongi.api.recipe.web.dto.RecipeStepsResponse;
import com.ongi.recipe.domain.Recipe;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class RecipeCacheReader {

	private final RecipeAdapter recipeAdapter;

	private final IngredientAdapter ingredientAdapter;

	@Cacheable(
		cacheNames = "recipeDetail",
		key = "'recipe:' + #recipeId + ':detail:' + #ver"
	)
	@Transactional(
		readOnly = true,
		transactionManager = "transactionManager"
	)
	public Recipe getRecipeById(Long recipeId, int ver) {
		return recipeAdapter.findRecipeById(recipeId).orElseThrow(() -> new IllegalStateException("Recipe not found"));
	}

	@Cacheable(
		cacheNames = "recipeIngredient",
		key = "'recipe:' + #recipeId + ':ingredients:' + #ver"
	)
	@Transactional(
		readOnly = true,
		transactionManager = "transactionManager"
	)
	public List<RecipeIngredientResponse> getRecipeIngredients(Long recipeId, int ver) {
		return ingredientAdapter.findRecipeIngredientByRecipeId(recipeId).stream()
			.map(RecipeDetailMapper::toIngredientResponse)
			.toList();
	}

	@Cacheable(
		cacheNames = "recipeSteps",
		key = "'recipe:' + #recipeId + ':steps:' + #ver"
	)
	@Transactional(
		readOnly = true,
		transactionManager = "transactionManager"
	)
	public List<RecipeStepsResponse> getRecipeSteps(Long recipeId, int ver) {
		return recipeAdapter.findRecipeStepsByRecipeId(recipeId).stream()
			.map(RecipeDetailMapper::toStepsResponse)
			.toList();
	}
}
