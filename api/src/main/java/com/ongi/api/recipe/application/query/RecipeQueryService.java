package com.ongi.api.recipe.application.query;

import com.ongi.api.recipe.adapter.out.persistence.RecipeAdapter;
import com.ongi.api.recipe.adapter.out.persistence.repository.RecipeBookmarkRepository;
import com.ongi.api.recipe.adapter.out.persistence.repository.RecipeLikeRepository;
import com.ongi.api.recipe.adapter.out.persistence.repository.RecipeStatsRepository;
import com.ongi.api.recipe.application.assembler.RecipeCommentAssembler;
import com.ongi.api.recipe.web.dto.CommentCreateRequest;
import com.ongi.api.recipe.web.dto.RecipeCommentItem;
import com.ongi.recipe.domain.RecipeComment;
import com.ongi.recipe.domain.enums.CommentSortOption;
import com.ongi.recipe.domain.enums.RecipeCommentStatus;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class RecipeQueryService {

	private final JPAQueryFactory queryFactory;

	private final RecipeCommentAssembler assembler;

	private final RecipeAdapter recipeAdapter;

	private final RecipeStatsRepository recipeStatsRepository;

	private final RecipeLikeRepository recipeLikeRepository;

	private final RecipeBookmarkRepository recipeBookmarkRepository;

	@Transactional(transactionManager = "transactionManager")
	public boolean like(long userId, long recipeId) {
		boolean inserted = recipeLikeRepository.insertIfNotExists(userId, recipeId);
		if (inserted) recipeStatsRepository.incrementLikeCount(recipeId, 1);
		return inserted;
	}

	@Transactional(transactionManager = "transactionManager")
	public boolean unlike(long userId, long recipeId) {
		boolean deleted = recipeLikeRepository.delete(userId, recipeId);
		if (deleted) recipeStatsRepository.incrementLikeCount(recipeId, -1);
		return deleted;
	}

	@Transactional(transactionManager = "transactionManager", readOnly = true)
	public long getRecipeLikeCount(long recipeId) {
		return recipeStatsRepository.findLikeCount(recipeId);
	}

	@Transactional(transactionManager = "transactionManager")
	public boolean bookmark(long userId, long recipeId) {
		boolean inserted = recipeBookmarkRepository.insertIfNotExists(userId, recipeId);
		if (inserted) recipeStatsRepository.incrementBookmarkCount(recipeId, 1);
		return inserted;
	}

	@Transactional(transactionManager = "transactionManager")
	public boolean unbookmark(long userId, long recipeId) {
		boolean deleted = recipeBookmarkRepository.delete(userId, recipeId);
		if (deleted) recipeStatsRepository.incrementBookmarkCount(recipeId, -1);
		return deleted;
	}

	@Transactional(transactionManager = "transactionManager", readOnly = true)
	public long getRecipeBookmarkCount(long recipeId) {
		return recipeStatsRepository.findBookmarkCount(recipeId);
	}

	@Transactional(transactionManager = "transactionManager")
	public Long createRecipeComment(long userId, long recipeId, CommentCreateRequest req) {
		// 1) 레시피 존재 검증
		if (!recipeAdapter.existsRecipeById(recipeId)) {
			throw new IllegalArgumentException("recipe not found: " + recipeId);
		}

		// 2) 엔티티 생성 (대댓글 정책)
		RecipeComment comment;
		if (req.parentId() == null) {
			comment = recipeAdapter.createRootComment(userId, recipeId, req.content());
		} else {
			RecipeComment parent = recipeAdapter
				.findRecipeCommentByIdAndRecipeId(req.parentId(), recipeId)
				.orElseThrow(() -> new IllegalArgumentException("parent not found"));

			if (parent.getStatus() != RecipeCommentStatus.ACTIVE) {
				throw new IllegalStateException("parent deleted");
			}

			long rootId = parent.getRootId();
			int depth = parent.getDepth() + 1;

			comment = recipeAdapter.createReplyComment(userId, recipeId, req.content(), rootId, req.parentId(), depth);
		}

		// 3) 카운트 즉시 반영 (upsert + atomic)
		recipeStatsRepository.upsertIncCommentCount(recipeId, +1);

		return comment.getId();
	}

	@Transactional(transactionManager = "transactionManager")
	public boolean deleteRecipeComment(long userId, long recipeId, long commentId) {
		RecipeComment comment = recipeAdapter
			.findRecipeCommentByIdAndRecipeId(commentId, recipeId)
			.orElseThrow(() -> new IllegalArgumentException("comment not found"));

		if (!comment.getUserId().equals(userId)) {
			throw new SecurityException("forbidden");
		}

		boolean deleted = recipeAdapter.deleteRecipeCommentSoft(comment);

		if(deleted) {
			recipeStatsRepository.upsertIncCommentCount(recipeId, -1);
		}

		return deleted;
	}

	@Transactional(transactionManager = "transactionManager", readOnly = true)
	public long getRecipeCommentCount(long recipeId) {
		return recipeStatsRepository.findCommentCount(recipeId);
	}

	@Transactional(transactionManager = "transactionManager", readOnly = true)
	public Page<RecipeCommentItem> getComments(Long recipeId, Pageable pageable, CommentSortOption sort) {
		return assembler.assemble(recipeId, pageable, sort);
	}

}
