package com.ongi.api.recipe.web;

import com.ongi.api.common.web.dto.ApiResponse;
import com.ongi.api.common.web.dto.Meta;
import com.ongi.api.recipe.application.RecipeService;
import com.ongi.api.recipe.web.dto.CursorPageRequest;
import com.ongi.api.recipe.web.dto.RecipeCardResponse;
import com.ongi.api.recipe.web.dto.RecipeDetailRequest;
import com.ongi.api.recipe.web.dto.RecipeDetailResponse;
import com.ongi.api.recipe.web.dto.RecipeSearchRequest;
import com.ongi.recipe.domain.search.RecipeSearch;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping("/recipe")
@RestController
public class RecipeController {

	private final RecipeService recipeService;

	@GetMapping("/public/v1/recipe/list")
	public ApiResponse<List<RecipeCardResponse>> getRecipes(
		@ModelAttribute CursorPageRequest cursorPageRequest,
		@ModelAttribute RecipeSearchRequest searchRequest
	) throws Exception {
		RecipeSearch search = searchRequest.toSearch();
		return recipeService.search(cursorPageRequest, search);
	}

	@GetMapping("/public/v1/recipe/{recipeId}")
	public ApiResponse<RecipeDetailResponse> getRecipeDetail(
		@ModelAttribute RecipeDetailRequest detailRequest,
		@PathVariable Long recipeId
	) throws Exception {

		// TODO user ID 기반 동작 추가

		return recipeService.getRecipeDetail(recipeId);
	}

	// TODO 오늘의 추천 레시피 ?

	// TODO 사용자 추천 레시피 ?

	// TODO 인기 레시피

	// TODO 내가 작성한 레시피

	// TODO 저장한 레시피

	// TODO 레시피 검색

	// TODO 인기 검색어

	// TODO 최근 검색어

	// TODO 레시피 업로드




}
