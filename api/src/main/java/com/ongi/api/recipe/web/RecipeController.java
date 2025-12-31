package com.ongi.api.recipe.web;

import com.ongi.api.common.web.dto.ApiResponse;
import com.ongi.api.common.web.dto.AuthPrincipal;
import com.ongi.api.recipe.adapter.out.persistence.metrics.projection.RelatedRecipeFinalRow;
import com.ongi.api.recipe.application.command.RecipeService;
import com.ongi.api.recipe.application.facade.RecipeEventFacade;
import com.ongi.api.recipe.application.query.RecipeQueryService;
import com.ongi.api.recipe.application.query.RecipeRelatedService;
import com.ongi.api.recipe.web.dto.BookmarkResponse;
import com.ongi.api.recipe.web.dto.CommentCreateRequest;
import com.ongi.api.recipe.web.dto.CommentCreateResponse;
import com.ongi.api.recipe.web.dto.CommentDeleteResponse;
import com.ongi.api.recipe.web.dto.CommentPageRequest;
import com.ongi.api.recipe.web.dto.CommentUpdateRequest;
import com.ongi.api.recipe.web.dto.CommentUpdateResponse;
import com.ongi.api.recipe.web.dto.CursorPageRequest;
import com.ongi.api.recipe.web.dto.LikeResponse;
import com.ongi.api.recipe.web.dto.RecipeCardResponse;
import com.ongi.api.recipe.web.dto.RecipeCommentItem;
import com.ongi.api.recipe.web.dto.RecipeDetailResponse;
import com.ongi.api.recipe.web.dto.RecipeUpsertRequest;
import com.ongi.api.recipe.web.dto.RecipeSearchRequest;
import com.ongi.api.recipe.web.dto.RelatedRecipeItem;
import com.ongi.recipe.domain.search.RecipeSearch;
import com.ongi.recipe.domain.search.RecipeSearchCondition;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping("/recipe")
@RestController
public class RecipeController {

	private final RecipeService recipeService;

	private final RecipeQueryService recipeQueryService;

	private final RecipeRelatedService recipeRelatedService;

	private final RecipeEventFacade recipeEventFacade;

	/**
	 * 레시피 목록 조회 API
	 * 키워드 / 태그 / 카테고리 / 영양소 ID / 조리 시간 기준으로 필터링 가능
	 * @param cursorPageRequest
	 * @param searchRequest
	 * @return
	 * @throws Exception
	 */
	@GetMapping("/public/recipe/list")
	public ApiResponse<List<RecipeCardResponse>> getRecipes(
		@ModelAttribute CursorPageRequest cursorPageRequest,
		@ModelAttribute RecipeSearchRequest searchRequest
	) throws Exception {
		RecipeSearch search = searchRequest.toSearch();
		RecipeSearchCondition condition = RecipeSearchCondition.from(search);
		return recipeService.search(cursorPageRequest, condition);
	}

	/**
	 * 연관 레시피 리스트 조회 By Popularity
	 * @param recipeId
	 * @param limit
	 * @return
	 */
	@GetMapping("/public/recipe/{recipeId}/related")
	public ApiResponse<List<RelatedRecipeItem>> getRecipesRelatedByBoost(
		@PathVariable Long recipeId,
		@RequestParam(defaultValue = "20") int limit
	) {
		// 정책으로 별도 정의
		int safeLimit = Math.min(limit, 50);

		return ApiResponse.ok(recipeRelatedService.findRelatedWithPopularityBoost(recipeId, safeLimit));
	}

	/**
	 * 레시피 디테일 조회
	 * @param authPrincipal
	 * @param recipeId
	 * @return
	 * @throws Exception
	 */
	@GetMapping("/public/recipe/{recipeId}")
	public ApiResponse<RecipeDetailResponse> getRecipeDetail(
		@AuthenticationPrincipal AuthPrincipal authPrincipal,
		@PathVariable Long recipeId
	) throws Exception {
		Long userId = (authPrincipal == null) ? null : authPrincipal.userId();
		// TODO 조회 시 사용자 활동 데이터 수집을 위한 체류 시간, 스크롤 Depth 등을 Event로 발송할 별도의 API 필요.
		/*
		"recipeId": 123,
	  "viewSessionId": "uuid",
	  "dwellMs": 18450,
	  "maxScrollDepth": 0.72,
	  "referrer": "feed",
	  "occurredAt": "2025-12-30T10:11:12Z"
	  */
		return ApiResponse.ok(recipeEventFacade.view(userId, recipeId));
	}

	/**
	 * 레시피 등록
	 * @param recipeUpsertRequest
	 * @return
	 * @throws Exception
	 */
	@PostMapping("/private/recipe")
	public ApiResponse<Void> createRecipe(
		@AuthenticationPrincipal AuthPrincipal authPrincipal,
		@RequestBody RecipeUpsertRequest recipeUpsertRequest
	) throws Exception {
		// TODO 확인할 것 - 등록한 레시피 기반으로 유사 레시피 추천?
		long userId = authPrincipal.userId();
		recipeEventFacade.createRecipe(userId, recipeUpsertRequest);
		return ApiResponse.ok();
	}

	/**
	 * 레시피 수정
	 * @param request
	 * @param authPrincipal
	 * @return
	 * @throws Exception
	 */
	@PatchMapping("/private/recipe")
	public ApiResponse<Void> updateRecipe(
		@AuthenticationPrincipal AuthPrincipal authPrincipal,
		@RequestBody RecipeUpsertRequest request
	) throws Exception {
		long userId = authPrincipal.userId();
		recipeEventFacade.updateRecipe(userId, request);
		return ApiResponse.ok();
	}

	/**
	 * 레시피 삭제
	 * @param recipeId
	 * @param authPrincipal
	 * @return
	 * @throws Exception
	 */
	@DeleteMapping("/private/recipe/{recipeId}")
	public ApiResponse<Void> deleteRecipe(
		@AuthenticationPrincipal AuthPrincipal authPrincipal,
		@PathVariable Long recipeId
	) throws Exception {
		long userId = authPrincipal.userId();
		recipeEventFacade.deleteRecipe(userId, recipeId);
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
		return ApiResponse.ok(recipeEventFacade.like(userId, recipeId));
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
		return ApiResponse.ok(recipeEventFacade.unlike(userId, recipeId));
	}

	/**
	 * 북마크 등록
	 * @param recipeId
	 * @param authPrincipal
	 * @return
	 * @throws Exception
	 */
	@PutMapping("/private/{recipeId}/bookmark")
	public ApiResponse<BookmarkResponse> bookmark(
		@PathVariable Long recipeId,
		@AuthenticationPrincipal AuthPrincipal authPrincipal
	) throws Exception {
		long userId = authPrincipal.userId();
		return ApiResponse.ok(recipeEventFacade.bookmark(userId, recipeId));
	}

	/**
	 * 북마크 삭제
	 * @param recipeId
	 * @param authPrincipal
	 * @return
	 * @throws Exception
	 */
	@DeleteMapping("/private/{recipeId}/bookmark")
	public ApiResponse<BookmarkResponse> unbookmark(
		@PathVariable Long recipeId,
		@AuthenticationPrincipal AuthPrincipal authPrincipal
	) throws Exception {
		long userId = authPrincipal.userId();
		return ApiResponse.ok(recipeEventFacade.unbookmark(userId, recipeId));
	}

	/**
	 * 레시피 댓글 등록
	 * @param auth
	 * @param recipeId
	 * @param req
	 * @return
	 */
	@PostMapping("/private/{recipeId}/comment")
	public ApiResponse<CommentCreateResponse> create(
		@AuthenticationPrincipal AuthPrincipal auth,
		@PathVariable long recipeId,
		@RequestBody @Valid CommentCreateRequest req
	) {
		long userId = auth.userId();
		return ApiResponse.ok(recipeEventFacade.createRecipeComment(userId, recipeId, req));
	}

	/**
	 * 레시피 댓글 수정
	 * @param auth
	 * @param recipeId
	 * @param commentId
	 * @param req
	 * @return
	 */
	@PatchMapping("/private/{recipeId}/comment/{commentId}")
	public ApiResponse<CommentUpdateResponse> update(
		@AuthenticationPrincipal AuthPrincipal auth,
		@PathVariable long recipeId,
		@PathVariable long commentId,
		@RequestBody @Valid CommentUpdateRequest req
	) {
		long userId = auth.userId();
		return ApiResponse.ok(recipeEventFacade.updateRecipeComment(userId, recipeId, commentId, req));
	}

	/**
	 * 레시피 댓글 삭제(Soft)
	 * @param auth
	 * @param recipeId
	 * @param commentId
	 * @return
	 */
	@DeleteMapping("/private/{recipeId}/comment/{commentId}")
	public ApiResponse<CommentDeleteResponse> delete(
		@AuthenticationPrincipal AuthPrincipal auth,
		@PathVariable long recipeId,
		@PathVariable long commentId
	) {
		long userId = auth.userId();
		return ApiResponse.ok(recipeEventFacade.deleteRecipeComment(userId, recipeId, commentId));
	}

	@GetMapping("/private/{recipeId}/comment")
	public ApiResponse<Page<RecipeCommentItem>> getComments(
		@PathVariable Long recipeId,
		@ModelAttribute CommentPageRequest req
	) {
		Pageable pageable = PageRequest.of(req.page(), req.size());
		return ApiResponse.ok(recipeQueryService.getComments(recipeId, pageable, req.resolveSort()));
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
