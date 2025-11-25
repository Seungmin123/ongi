package com.ongi.recipe.port;

import com.ongi.recipe.domain.Recipe;
import com.ongi.recipe.domain.RecipeSteps;
import com.ongi.recipe.domain.RecipeTags;
import java.util.Optional;

public interface RecipeRepositoryPort {

	Recipe save(Recipe recipe);

	Optional<Recipe> findRecipeById(Long id);

	RecipeSteps save(RecipeSteps recipeSteps);

	Optional<RecipeSteps> findRecipeStepsById(Long id);

	RecipeTags save(RecipeTags recipeTags);

	Optional<RecipeTags> findRecipeTagsById(Long id);
}
