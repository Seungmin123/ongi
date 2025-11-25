package com.ongi.api.recipe.persistence;

import com.ongi.api.recipe.persistence.repository.RecipeRepository;
import com.ongi.api.recipe.persistence.repository.RecipeStepsRepository;
import com.ongi.api.recipe.persistence.repository.RecipeTagsRepository;
import com.ongi.recipe.domain.Recipe;
import com.ongi.recipe.domain.RecipeSteps;
import com.ongi.recipe.domain.RecipeTags;
import com.ongi.recipe.port.RecipeRepositoryPort;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@Repository
public class RecipeAdapter implements RecipeRepositoryPort {

	private final RecipeRepository recipeRepository;

	private final RecipeStepsRepository recipeStepsRepository;

	private final RecipeTagsRepository recipeTagsRepository;

	@Override
	public Recipe save(Recipe recipe) {
		RecipeEntity entity = RecipeMapper.toEntity(recipe);
		RecipeEntity saved = recipeRepository.save(entity);
		return RecipeMapper.toDomain(saved);
	}

	@Override
	public Optional<Recipe> findRecipeById(Long id) {
		return recipeRepository
			.findById(id)
			.map(RecipeMapper::toDomain);
	}

	@Override
	public RecipeSteps save(RecipeSteps recipeSteps) {
		RecipeStepsEntity entity = RecipeMapper.toEntity(recipeSteps);
		RecipeStepsEntity saved = recipeStepsRepository.save(entity);
		return RecipeMapper.toDomain(saved);
	}

	@Override
	public Optional<RecipeSteps> findRecipeStepsById(Long id) {
		return recipeStepsRepository
			.findById(id)
			.map(RecipeMapper::toDomain);
	}

	@Override
	public RecipeTags save(RecipeTags recipeTags) {
		RecipeTagsEntity entity = RecipeMapper.toEntity(recipeTags);
		RecipeTagsEntity saved = recipeTagsRepository.save(entity);
		return RecipeMapper.toDomain(saved);
	}

	@Override
	public Optional<RecipeTags> findRecipeTagsById(Long id) {
		return recipeTagsRepository
			.findById(id)
			.map(RecipeMapper::toDomain);
	}
}
