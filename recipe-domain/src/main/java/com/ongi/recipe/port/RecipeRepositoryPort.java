package com.ongi.recipe.port;

import com.ongi.recipe.domain.Recipe;
import com.ongi.recipe.domain.RecipeSteps;
import com.ongi.recipe.domain.RecipeTags;
import com.ongi.recipe.domain.enums.PageSortOptionEnum;
import com.ongi.recipe.domain.search.RecipeSearchCondition;
import java.util.List;
import java.util.Optional;

public interface RecipeRepositoryPort {

	Recipe save(Recipe recipe);

	Optional<Recipe> findRecipeById(Long id);

	RecipeSteps save(RecipeSteps recipeSteps);

	List<RecipeSteps> saveAllRecipeSteps(List<RecipeSteps> recipeSteps);

	Optional<RecipeSteps> findRecipeStepsById(Long id);

	List<RecipeSteps> findRecipeStepsByRecipeId(Long id);

	RecipeTags save(RecipeTags recipeTags);

	List<RecipeTags> saveAllRecipeTags(List<RecipeTags> recipeSteps);

	Optional<RecipeTags> findRecipeTagsById(Long id);

	List<Recipe> search(RecipeSearchCondition condition, Long cursor, Integer size, PageSortOptionEnum sort);
}
