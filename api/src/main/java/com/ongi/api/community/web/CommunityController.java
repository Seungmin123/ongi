package com.ongi.api.community.web;

import com.ongi.api.common.web.dto.ApiResponse;
import com.ongi.api.common.web.dto.AuthPrincipal;
import com.ongi.api.community.application.command.AttachmentAttachService;
import com.ongi.api.community.application.facade.CommunityCommentEventFacade;
import com.ongi.api.community.application.facade.CommunityPostEventFacade;
import com.ongi.api.community.application.query.CommentQueryService;
import com.ongi.api.community.application.query.PostQueryService;
import com.ongi.api.community.web.dto.CommentItem;
import com.ongi.api.community.web.dto.CommentUpsertRequest;
import com.ongi.api.community.web.dto.CreateAttachmentUploadUrlRequest;
import com.ongi.api.community.web.dto.CreateAttachmentUploadUrlResponse;
import com.ongi.api.community.web.dto.CreateTempAttachmentRequest;
import com.ongi.api.community.web.dto.CreateTempAttachmentResponse;
import com.ongi.api.community.web.dto.LikeResponse;
import com.ongi.api.community.web.dto.PostCardItem;
import com.ongi.api.community.web.dto.PostDetailResponse;
import com.ongi.api.community.web.dto.ListRequest;
import com.ongi.api.community.web.dto.PostUpsertRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping("/community")
@RestController
public class CommunityController {

	private final CommunityPostEventFacade postEventFacade;

	private final PostQueryService postQueryService;

	private final CommunityCommentEventFacade commentEventFacade;

	private final CommentQueryService commentQueryService;

	private final AttachmentAttachService attachmentAttachService;

	@PostMapping("/private/upload-url")
	public ApiResponse<CreateAttachmentUploadUrlResponse> createUploadUrl(
		@AuthenticationPrincipal AuthPrincipal authPrincipal,
		@RequestBody @Valid CreateAttachmentUploadUrlRequest req
	) {
		return ApiResponse.ok(attachmentAttachService.createUploadUrl(authPrincipal.userId(), req));
	}

	@PostMapping("/private/upload-url-temp")
	public ApiResponse<CreateTempAttachmentResponse> createTemp(
		@AuthenticationPrincipal AuthPrincipal authPrincipal,
		@RequestBody @Valid CreateTempAttachmentRequest req
	) {
		return ApiResponse.ok(attachmentAttachService.createTemp(authPrincipal.userId(), req));
	}

	/**
	 * 커뮤니티 게시글 등록
	 * @param authPrincipal
	 * @param req
	 * @return
	 * @throws Exception
	 */
	@PostMapping("/private/posts")
	public ApiResponse<Void> createPost(
		@AuthenticationPrincipal AuthPrincipal authPrincipal,
		@RequestBody @Valid PostUpsertRequest req
	) throws Exception {
		Long userId = authPrincipal.userId();
		postEventFacade.createPost(userId, req);
		return ApiResponse.ok();
	}

	/**
	 * 커뮤니티 게시글 수정
	 * @param authPrincipal
	 * @param req
	 * @return
	 * @throws Exception
	 */
	@PatchMapping("/private/posts/{postId}")
	public ApiResponse<Void> updatePost(
		@AuthenticationPrincipal AuthPrincipal authPrincipal,
		@RequestBody @Valid PostUpsertRequest req
	) throws Exception {
		Long userId = authPrincipal.userId();
		postEventFacade.updatePost(userId, req);
		return ApiResponse.ok();
	}

	/**
	 * 커뮤니티 게시글 삭제(Soft)
	 * @param authPrincipal
	 * @param postId
	 * @return
	 * @throws Exception
	 */
	@DeleteMapping("/private/posts/{postId}")
	public ApiResponse<Void> deletePost(
		@AuthenticationPrincipal AuthPrincipal authPrincipal,
		@PathVariable Long postId
	) throws Exception {
		Long userId = authPrincipal.userId();
		postEventFacade.deletePost(userId, postId);
		return ApiResponse.ok();
	}

	/**
	 * 커뮤니티 게시글 목록 조회
	 * @param authPrincipal
	 * @param req
	 * @return
	 * @throws Exception
	 */
	@GetMapping("/public/posts/list")
	public ApiResponse<Page<PostCardItem>> getPosts(
		@AuthenticationPrincipal AuthPrincipal authPrincipal,
		@ModelAttribute ListRequest req
	) throws Exception {
		Long userId = (authPrincipal == null) ? null : authPrincipal.userId();
		Page<PostCardItem> items = postQueryService.getPosts(userId,
			PageRequest.of(
				req.resolvedPage(),
				req.resolvedSize()
			),
			req.resolvePostSort()
		);
		return ApiResponse.ok(items);
	}

	/**
	 * 커뮤니티 게시글 상세 정보 조회
	 * @param authPrincipal
	 * @param postId
	 * @return
	 * @throws Exception
	 */
	@GetMapping("/public/posts/{postId}")
	public ApiResponse<PostDetailResponse> getPost(
		@AuthenticationPrincipal AuthPrincipal authPrincipal,
		@PathVariable Long postId
	) throws Exception {
		Long userId = (authPrincipal == null) ? null : authPrincipal.userId();
		return ApiResponse.ok(postEventFacade.getPost(userId, postId));
	}

	/**
	 * 커뮤니티 게시글 좋아요 등록
	 * @param authPrincipal
	 * @param postId
	 * @return
	 * @throws Exception
	 */
	@PostMapping("/private/posts/{postId}/like")
	public ApiResponse<LikeResponse> like(
		@AuthenticationPrincipal AuthPrincipal authPrincipal,
		@PathVariable Long postId
	) throws Exception {
		Long userId = authPrincipal.userId();
		return ApiResponse.ok(postEventFacade.like(userId, postId));
	}

	/**
	 * 커뮤니티 게시글 좋아요 삭제
	 * @param authPrincipal
	 * @param postId
	 * @return
	 * @throws Exception
	 */
	@DeleteMapping("/private/posts/{postId}/unlike")
	public ApiResponse<LikeResponse> unlike(
		@AuthenticationPrincipal AuthPrincipal authPrincipal,
		@PathVariable Long postId
	) throws Exception {
		Long userId = authPrincipal.userId();
		return ApiResponse.ok(postEventFacade.unlike(userId, postId));
	}

	/**
	 * 커뮤니티 댓글 등록
	 * @param authPrincipal
	 * @param postId
	 * @param req
	 * @return
	 * @throws Exception
	 */
	@PostMapping("/private/posts/{postId}/comments")
	public ApiResponse<Void> createPostComment(
		@AuthenticationPrincipal AuthPrincipal authPrincipal,
		@PathVariable Long postId,
		@RequestBody @Valid CommentUpsertRequest req
	) throws Exception {
		Long userId = authPrincipal.userId();
		commentEventFacade.createComment(userId, postId, req);
		return ApiResponse.ok();
	}

	/**
	 * 커뮤니티 댓글 수정
	 * @param authPrincipal
	 * @param postId
	 * @param commentId
	 * @param req
	 * @return
	 * @throws Exception
	 */
	@PatchMapping("/private/posts/{postId}/comments/{commentId}")
	public ApiResponse<Void> updatePostComment(
		@AuthenticationPrincipal AuthPrincipal authPrincipal,
		@PathVariable Long postId,
		@PathVariable Long commentId,
		@RequestBody @Valid CommentUpsertRequest req
	) throws Exception {
		Long userId = authPrincipal.userId();
		commentEventFacade.updateComment(userId, postId, commentId, req);
		return ApiResponse.ok();
	}

	/**
	 * 커뮤니티 댓글 삭제
	 * @param authPrincipal
	 * @param postId
	 * @param commentId
	 * @return
	 * @throws Exception
	 */
	@DeleteMapping("/private/posts/{postId}/comments/{commentId}")
	public ApiResponse<Void> deletePostComment(
		@AuthenticationPrincipal AuthPrincipal authPrincipal,
		@PathVariable Long postId,
		@PathVariable Long commentId
	) throws Exception {
		Long userId = authPrincipal.userId();
		commentEventFacade.deleteComment(userId, postId, commentId);
		return ApiResponse.ok();
	}

	/**
	 * 커뮤니티 댓글 목록 조회
	 * @param authPrincipal
	 * @param req
	 * @param postId
	 * @return
	 * @throws Exception
	 */
	@GetMapping("/public/posts/{postId}/comments/list")
	public ApiResponse<Page<CommentItem>> getPostComments(
		@AuthenticationPrincipal AuthPrincipal authPrincipal,
		@ModelAttribute @Valid ListRequest req,
		@PathVariable Long postId
	) throws Exception {
		Long userId = (authPrincipal == null) ? null : authPrincipal.userId();
		Page<CommentItem> items = commentQueryService.getComments(
			userId,
			postId,
			PageRequest.of(
				req.resolvedPage(),
				req.resolvedSize()
			),
			req.resolveCommentSort()
		);
		return ApiResponse.ok(items);
	}

	/**
	 * 커뮤니티 댓글 좋아요 등록
	 * @param authPrincipal
	 * @param postId
	 * @param commentId
	 * @return
	 * @throws Exception
	 */
	@PostMapping("/private/posts/{postId}/comments/{commentId}/like")
	public ApiResponse<LikeResponse> likeComment(
		@AuthenticationPrincipal AuthPrincipal authPrincipal,
		@PathVariable Long postId,
		@PathVariable Long commentId
	) throws Exception {
		Long userId = authPrincipal.userId();
		return ApiResponse.ok(commentEventFacade.like(userId, postId, commentId));
	}

	/**
	 * 커뮤니티 댓글 좋아요 삭제
	 * @param authPrincipal
	 * @param postId
	 * @param commentId
	 * @return
	 * @throws Exception
	 */
	@DeleteMapping("/private/posts/{postId}/comments/{commentId}/unlike")
	public ApiResponse<LikeResponse> unlikeComment(
		@AuthenticationPrincipal AuthPrincipal authPrincipal,
		@PathVariable Long postId,
		@PathVariable Long commentId
	) throws Exception {
		Long userId = authPrincipal.userId();
		return ApiResponse.ok(commentEventFacade.unlike(userId, postId, commentId));
	}

}
