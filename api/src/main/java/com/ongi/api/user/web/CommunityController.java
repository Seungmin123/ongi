package com.ongi.api.user.web;

import com.ongi.api.common.web.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping("/community")
@RestController
public class CommunityController {

	// TODO 커뮤니티 게시물 등록 private
	@PostMapping("/private/posts")
	public ApiResponse<Void> createPost() throws Exception {
		return ApiResponse.ok();
	}

	// TODO 커뮤니티 게시물 수정 private
	@PatchMapping("/private/posts/{postId}")
	public ApiResponse<Void> updatePost() throws Exception {
		return ApiResponse.ok();
	}

	// TODO 커뮤니티 게시물 삭제 private
	@DeleteMapping("/private/posts/{postId}")
	public ApiResponse<Void> deletePost() throws Exception {
		return ApiResponse.ok();
	}

	// TODO 커뮤니티 게시물 리스트 조회 public
	@GetMapping("/public/posts/list")
	public ApiResponse<Void> getPosts() throws Exception {
		return ApiResponse.ok();
	}

	// TODO 커뮤니티 게시물 디테일 조회 public
	@GetMapping("/public/posts/{postId}")
	public ApiResponse<Void> getPostDetail() throws Exception {
		return ApiResponse.ok();
	}

	// TODO 커뮤니티 게시글 좋아요 등록 private
	@PostMapping("/private/posts/{postId}/like")
	public ApiResponse<Void> likePost() throws Exception {
		return ApiResponse.ok();
	}

	// TODO 커뮤니티 게시글 좋아요 삭제 private
	@DeleteMapping("/private/posts/{postId}/unlike")
	public ApiResponse<Void> unlikePost() throws Exception {
		return ApiResponse.ok();
	}

	// TODO 커뮤니티 댓글 등록 private
	@PostMapping("/private/posts/{postId}/comments")
	public ApiResponse<Void> createPostComment() throws Exception {
		return ApiResponse.ok();
	}

	// TODO 커뮤니티 댓글 수정 private
	@PatchMapping("/private/posts/{postId}/comments/{commentId}")
	public ApiResponse<Void> updatePostComment() throws Exception {
		return ApiResponse.ok();
	}

	// TODO 커뮤니티 댓글 삭제 private
	@DeleteMapping("/private/posts/{postId}/comments/{commentId}")
	public ApiResponse<Void> deletePostComment() throws Exception {
		return ApiResponse.ok();
	}

	// TODO 커뮤니티 댓글 조회 public
	@GetMapping("/private/posts/{postId}/comments/list")
	public ApiResponse<Void> getPostComments() throws Exception {
		return ApiResponse.ok();
	}

	// TODO 커뮤니티 댓글 좋아요 등록 private
	@PostMapping("/private/posts/{postId}/comments/{commentId}/like")
	public ApiResponse<Void> likeComment() throws Exception {
		return ApiResponse.ok();
	}

	// TODO 커뮤니티 댓글 좋아요 삭제 private
	@DeleteMapping("/private/posts/{postId}/comments/{commentId}/unlike")
	public ApiResponse<Void> unlikeComment() throws Exception {
		return ApiResponse.ok();
	}

}
