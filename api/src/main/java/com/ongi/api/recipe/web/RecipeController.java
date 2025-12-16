package com.ongi.api.recipe.web;

import com.ongi.api.common.web.dto.ApiResponse;
import com.ongi.api.config.aspect.CurrentToken;
import com.ongi.api.recipe.application.RecipeService;
import com.ongi.api.recipe.web.dto.CursorPageRequest;
import com.ongi.api.recipe.web.dto.RecipeCardResponse;
import com.ongi.api.recipe.web.dto.RecipeUpsertRequest;
import com.ongi.api.recipe.web.dto.RecipeDetailRequest;
import com.ongi.api.recipe.web.dto.RecipeDetailResponse;
import com.ongi.api.recipe.web.dto.RecipeSearchRequest;
import com.ongi.recipe.domain.search.RecipeSearch;
import com.ongi.recipe.domain.search.RecipeSearchCondition;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping("/recipe")
@RestController
public class RecipeController {

	private final RecipeService recipeService;

	/**
	 * 레시피 목록 조회 API
	 * 키워드 / 태그 / 카테고리 / 영양소 ID / 조리 시간 기준으로 필터링 가능
	 * @param cursorPageRequest
	 * @param searchRequest
	 * @return
	 * @throws Exception
	 */
	@GetMapping("/public/v1/recipe/list")
	public ApiResponse<List<RecipeCardResponse>> getRecipes(
		@ModelAttribute CursorPageRequest cursorPageRequest,
		@ModelAttribute RecipeSearchRequest searchRequest
	) throws Exception {
		RecipeSearch search = searchRequest.toSearch();
		RecipeSearchCondition condition = RecipeSearchCondition.from(search);
		return recipeService.search(cursorPageRequest, condition);
	}

	/**
	 * 레시피 디테일 조회
	 * @param detailRequest
	 * @param recipeId
	 * @return
	 * @throws Exception
	 */
	@GetMapping("/public/v1/recipe/{recipeId}")
	public ApiResponse<RecipeDetailResponse> getRecipeDetail(
		@ModelAttribute RecipeDetailRequest detailRequest,
		@PathVariable Long recipeId
	) throws Exception {

		// TODO user ID 기반 동작 추가

		return recipeService.getRecipeDetail(recipeId);
	}

	/**
	 * 레시피 등록
	 * @param recipeUpsertRequest
	 * @return
	 * @throws Exception
	 */
	@PostMapping("/private/v1/recipe")
	public ApiResponse<Void> createRecipe(
		@ModelAttribute RecipeUpsertRequest recipeUpsertRequest
	) throws Exception {
		// TODO 확인할 것 - 등록한 레시피 기반으로 유사 레시피 추천?
		// TODO Kafka User Event 발행?
		recipeService.createRecipe(recipeUpsertRequest);
		return ApiResponse.ok();
	}

	/**
	 * 레시피 수정
	 * @param request
	 * @return
	 * @throws Exception
	 */
	@PatchMapping("/private/v1/recipe")
	public ApiResponse<Void> updateRecipe(
		@ModelAttribute RecipeUpsertRequest request
	) throws Exception {
		// TODO Kafka User Event 발행?
		recipeService.updateRecipe(request);
		return ApiResponse.ok();
	}

	/**
	 * 레시피 삭제
	 * @param recipeId
	 * @param jwt
	 * @return
	 * @throws Exception
	 */
	@DeleteMapping("/private/v1/recipe/{recipeId}")
	public ApiResponse<Void> deleteRecipe(
		@PathVariable Long recipeId,
		@CurrentToken String jwt
	) throws Exception {
		// TODO JWT 관련 Aspect, Token Provider 수정

		// TODO JWT 와 Recipe UserId 검증

		// TODO Kafka User Event 발행?

		recipeService.deleteRecipe(recipeId);
		return ApiResponse.ok();
	}

	// TODO 오늘의 추천 레시피 ?

	// TODO 사용자 추천 레시피 ?

	// TODO 인기 레시피

	// TODO 내가 작성한 레시피

	// TODO 내가 저장한 레시피

	// TODO 레시피 검색

	// TODO 인기 검색어

	// TODO 최근 검색어


}
