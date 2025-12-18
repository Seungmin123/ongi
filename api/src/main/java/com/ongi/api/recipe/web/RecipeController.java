package com.ongi.api.recipe.web;

import com.ongi.api.common.web.dto.ApiResponse;
import com.ongi.api.common.web.dto.AuthPrincipal;
import com.ongi.api.recipe.application.RecipeService;
import com.ongi.api.recipe.application.facade.RecipeEventFacade;
import com.ongi.api.recipe.web.dto.CommentCreateRequest;
import com.ongi.api.recipe.web.dto.CommentCreateResponse;
import com.ongi.api.recipe.web.dto.CommentDeleteResponse;
import com.ongi.api.recipe.web.dto.CommentUpdateRequest;
import com.ongi.api.recipe.web.dto.CommentUpdateResponse;
import com.ongi.api.recipe.web.dto.CursorPageRequest;
import com.ongi.api.recipe.web.dto.LikeResponse;
import com.ongi.api.recipe.web.dto.RecipeCardResponse;
import com.ongi.api.recipe.web.dto.RecipeDetailResponse;
import com.ongi.api.recipe.web.dto.RecipeUpsertRequest;
import com.ongi.api.recipe.web.dto.RecipeSearchRequest;
import com.ongi.recipe.domain.search.RecipeSearch;
import com.ongi.recipe.domain.search.RecipeSearchCondition;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping("/recipe")
@RestController
public class RecipeController {

	private final RecipeService recipeService;

	private final RecipeEventFacade recipeEventFacade;

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
	 * @param authPrincipal
	 * @param recipeId
	 * @return
	 * @throws Exception
	 */
	@GetMapping("/public/v1/recipe/{recipeId}")
	public ApiResponse<RecipeDetailResponse> getRecipeDetail(
		@AuthenticationPrincipal AuthPrincipal authPrincipal,
		@PathVariable Long recipeId
	) throws Exception {
		Long userId = (authPrincipal == null) ? null : authPrincipal.userId();
		return ApiResponse.ok(recipeEventFacade.view(recipeId, userId));
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
	 * @param authPrincipal
	 * @return
	 * @throws Exception
	 */
	@PatchMapping("/private/v1/recipe")
	public ApiResponse<Void> updateRecipe(
		@AuthenticationPrincipal AuthPrincipal authPrincipal,
		@ModelAttribute RecipeUpsertRequest request
	) throws Exception {
		// TODO Kafka User Event 발행?

		// TODO userId 와 Recipe UserId 검증

		recipeService.updateRecipe(request);
		return ApiResponse.ok();
	}

	/**
	 * 레시피 삭제
	 * @param recipeId
	 * @param authPrincipal
	 * @return
	 * @throws Exception
	 */
	@DeleteMapping("/private/v1/recipe/{recipeId}")
	public ApiResponse<Void> deleteRecipe(
		@AuthenticationPrincipal AuthPrincipal authPrincipal,
		@PathVariable Long recipeId
	) throws Exception {
		// TODO userId 와 Recipe UserId 검증

		// TODO Kafka User Event 발행?

		recipeService.deleteRecipe(recipeId);
		return ApiResponse.ok();
	}

	/**
	 * 좋아요 등록
	 * @param recipeId
	 * @param authPrincipal
	 * @return
	 * @throws Exception
	 */
	@PutMapping("/private/{recipeId}/like")
	public ApiResponse<LikeResponse> like(
		@PathVariable Long recipeId,
		@AuthenticationPrincipal AuthPrincipal authPrincipal
	) throws Exception {
		long userId = authPrincipal.userId();
		return ApiResponse.ok(recipeEventFacade.like(recipeId, userId));
	}

	/**
	 * 좋아요 삭제
	 * @param recipeId
	 * @param authPrincipal
	 * @return
	 * @throws Exception
	 */
	@DeleteMapping("/private/{recipeId}/like")
	public ApiResponse<LikeResponse> unlike(
		@PathVariable Long recipeId,
		@AuthenticationPrincipal AuthPrincipal authPrincipal
	) throws Exception {
		long userId = authPrincipal.userId();
		return ApiResponse.ok(recipeEventFacade.unlike(recipeId, userId));
	}

	@PostMapping("/private/v1/recipes/{recipeId}/comments")
	public ApiResponse<CommentCreateResponse> create(
		@AuthenticationPrincipal AuthPrincipal auth,
		@PathVariable long recipeId,
		@RequestBody @Valid CommentCreateRequest req
	) {
		long userId = auth.userId();
		return ApiResponse.ok(recipeEventFacade.createRecipeComment(recipeId, userId, req));
	}

	@PatchMapping("/private/v1/recipes/{recipeId}/comments/{commentId}")
	public ApiResponse<CommentUpdateResponse> update(
		@AuthenticationPrincipal AuthPrincipal auth,
		@PathVariable long recipeId,
		@PathVariable long commentId,
		@RequestBody @Valid CommentUpdateRequest req
	) {
		long userId = auth.userId();
		return ApiResponse.ok(recipeEventFacade.updateRecipeComment(recipeId, userId, commentId, req));
	}

	@DeleteMapping("/private/v1/recipes/{recipeId}/comments/{commentId}")
	public ApiResponse<CommentDeleteResponse> delete(
		@AuthenticationPrincipal AuthPrincipal auth,
		@PathVariable long recipeId,
		@PathVariable long commentId
	) {
		long userId = auth.userId();
		return ApiResponse.ok(recipeEventFacade.deleteRecipeComment(recipeId, userId, commentId));
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
