package com.ongi.api.recipe.application.assembler;

import com.ongi.api.recipe.adapter.out.persistence.QRecipeCommentEntity;
import com.ongi.api.recipe.port.UserInfoProvider;
import com.ongi.api.recipe.web.dto.CommentRow;
import com.ongi.recipe.domain.enums.CommentSortOption;
import com.ongi.recipe.domain.enums.RecipeCommentStatus;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

@Component
public class RecipeCommentAssembler extends AbstractCommentAssembler {

	private final JPAQueryFactory queryFactory;

	public RecipeCommentAssembler(UserInfoProvider userInfoProvider, JPAQueryFactory queryFactory) {
		super(userInfoProvider);
		this.queryFactory = queryFactory;
	}

	@Override
	protected Page<CommentRow> fetchComments(Long recipeId, Pageable pageable, CommentSortOption sort) {
		QRecipeCommentEntity c = QRecipeCommentEntity.recipeCommentEntity;

		var order = (sort == CommentSortOption.OLDEST)
			? new OrderSpecifier[]{
			new OrderSpecifier<>(Order.ASC, c.createdAt),
			new OrderSpecifier<>(Order.ASC, c.id)
		}
			: new OrderSpecifier[]{
				new OrderSpecifier<>(Order.DESC, c.createdAt),
				new OrderSpecifier<>(Order.DESC, c.id)
			};

		List<CommentRow> content = queryFactory
			.select(Projections.constructor(
				CommentRow.class,
				c.id, c.rootId, c.parentId, c.depth,
				c.userId, c.content, c.status, c.createdAt
			))
			.from(c)
			.where(c.recipeId.eq(recipeId)
				.and(c.status.ne(RecipeCommentStatus.BLOCKED)))
			.orderBy(order)
			.offset(pageable.getOffset())
			.limit(pageable.getPageSize())
			.fetch();

		Long total = queryFactory
			.select(c.count())
			.from(c)
			.where(c.recipeId.eq(recipeId)
				.and(c.status.ne(RecipeCommentStatus.BLOCKED)))
			.fetchOne();

		return new PageImpl<>(content, pageable, total == null ? 0 : total);
	}
}
