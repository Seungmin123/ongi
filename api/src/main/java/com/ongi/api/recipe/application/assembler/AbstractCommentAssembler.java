package com.ongi.api.recipe.application.assembler;

import com.ongi.api.recipe.port.UserInfoProvider;
import com.ongi.api.recipe.web.dto.CommentRow;
import com.ongi.api.recipe.web.dto.RecipeCommentItem;
import com.ongi.api.recipe.web.dto.UserSummary;
import com.ongi.recipe.domain.enums.CommentSortOption;
import com.ongi.recipe.domain.enums.RecipeCommentStatus;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

@RequiredArgsConstructor
public abstract class AbstractCommentAssembler {

	private final UserInfoProvider userInfoProvider;

	public final Page<RecipeCommentItem> assemble(Long recipeId, Pageable pageable, CommentSortOption sort) {
		var page = fetchComments(recipeId, pageable, sort);

		var authorIds = page.getContent().stream()
			.map(CommentRow::userId)
			.filter(java.util.Objects::nonNull)
			.collect(java.util.stream.Collectors.toSet());

		var users = userInfoProvider.getUsersByIds(authorIds);

		var items = page.getContent().stream()
			.map(row -> merge(row, users.get(row.userId())))
			.toList();

		return new PageImpl<>(items, pageable, page.getTotalElements());
	}

	protected abstract Page<CommentRow> fetchComments(Long recipeId, Pageable pageable, CommentSortOption sort);

	private RecipeCommentItem merge(CommentRow c, UserSummary u) {
		boolean deleted = (c.status() == RecipeCommentStatus.DELETED);

		return new RecipeCommentItem(
			c.commentId(),
			c.rootId(),
			c.parentId(),
			c.depth(),
			deleted ? "삭제된 댓글입니다." : c.content(),
			u,
			c.createdAt()
		);
	}
}
