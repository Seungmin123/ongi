package com.ongi.recipe.port;

import com.ongi.recipe.domain.Recipe;
import com.ongi.recipe.domain.RecipeBookmark;
import com.ongi.recipe.domain.RecipeComment;
import com.ongi.recipe.domain.RecipeLike;
import com.ongi.recipe.domain.RecipeStats;
import com.ongi.recipe.domain.RecipeSteps;
import com.ongi.recipe.domain.RecipeTags;
import com.ongi.recipe.domain.RecipeUserFlags;
import com.ongi.recipe.domain.enums.PageSortOptionEnum;
import com.ongi.recipe.domain.enums.RecipeCommentStatus;
import com.ongi.recipe.domain.search.RecipeSearchCondition;
import java.util.List;
import java.util.Optional;

public interface RecipeRepositoryPort {

	Recipe save(Recipe recipe);

	Optional<Recipe> findRecipeById(Long id);

	boolean existsRecipeById(Long id);

	void deleteRecipeById(Long id);

	RecipeSteps save(RecipeSteps recipeSteps);

	List<RecipeSteps> saveAllRecipeSteps(List<RecipeSteps> recipeSteps);

	Optional<RecipeSteps> findRecipeStepsById(Long id);

	List<RecipeSteps> findRecipeStepsByRecipeId(Long id);

	void deleteRecipeStepsByRecipeId(Long recipeId);

	RecipeTags save(RecipeTags recipeTags);

	List<RecipeTags> saveAllRecipeTags(List<RecipeTags> recipeSteps);

	Optional<RecipeTags> findRecipeTagsById(Long id);

	List<Recipe> search(RecipeSearchCondition condition, Long cursor, Integer size, PageSortOptionEnum sort);

	RecipeLike save(RecipeLike recipeLike);

	RecipeBookmark save(RecipeBookmark recipeLike);

	RecipeUserFlags getFlags(Long userId, Long recipeId);

	RecipeStats save(RecipeStats recipeStats);

	Optional<RecipeStats> findRecipeStatsByRecipeId(Long recipeId);

	List<RecipeStats> findRecipeStatsByRecipeIds(List<Long> recipeIds);

	RecipeComment save(RecipeComment recipeComment);

	boolean existsRecipeCommentById(Long id);

	Optional<RecipeComment> findRecipeCommentByIdAndRecipeId(Long id, Long recipeId);

	Optional<RecipeComment> findRecipeCommentByIdAndRecipeIdAndStatus(Long id, Long recipeId, RecipeCommentStatus status);

	RecipeComment createRootComment(Long userId, Long recipeId, String content);

	RecipeComment createReplyComment(Long userId, Long recipeId, String content, Long rootId, Long parentId, int depth);

	RecipeComment updateRecipeCommentContent(RecipeComment domain, String content);

	boolean deleteRecipeCommentSoft(RecipeComment domain);

}
