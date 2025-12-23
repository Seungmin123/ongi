package com.ongi.api.community.application.assembler;

import com.ongi.api.community.adatper.out.persistence.QCommunityAttachmentEntity;
import com.ongi.api.community.adatper.out.persistence.QCommunityCommentEntity;
import com.ongi.api.community.adatper.out.persistence.QCommunityPostEntity;
import com.ongi.api.community.adatper.out.persistence.QCommunityPostStatsEntity;
import com.ongi.api.community.adatper.out.persistence.enums.AttachmentStatus;
import com.ongi.api.community.adatper.out.persistence.enums.CommentSortOption;
import com.ongi.api.community.adatper.out.persistence.enums.CommentStatus;
import com.ongi.api.community.adatper.out.persistence.enums.PostStatus;
import com.ongi.api.community.adatper.out.persistence.repository.CommunityCommentLikeRepository;
import com.ongi.api.community.adatper.out.persistence.repository.CommunityPostLikeRepository;
import com.ongi.api.community.adatper.out.user.UserInfoProvider;
import com.ongi.api.community.web.dto.CommentRow;
import com.ongi.api.community.web.dto.PostAttachmentRow;
import com.ongi.api.community.web.dto.PostCardRow;
import com.ongi.api.community.web.dto.PostDetailRow;
import com.ongi.api.user.application.component.FileClient;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

@Component
public class CommentListAssembler extends AbstractCommentListAssembler {

	private final JPAQueryFactory queryFactory;

	public CommentListAssembler(
		UserInfoProvider userInfoProvider,
		CommunityCommentLikeRepository commentLikeRepository,
		JPAQueryFactory queryFactory) {
		super(userInfoProvider, commentLikeRepository);
		this.queryFactory = queryFactory;
	}

	@Override
	protected Page<CommentRow> fetchComments(Long postId, Pageable pageable, CommentSortOption sort) {
		QCommunityCommentEntity c = QCommunityCommentEntity.communityCommentEntity;

		OrderSpecifier<?>[] order = switch (sort) {
			case CREATED_ASC -> new OrderSpecifier<?>[]{ c.createdAt.asc(), c.id.asc() };
			case CREATED_DESC -> new OrderSpecifier<?>[]{ c.createdAt.desc(), c.id.desc() };
		};

		List<CommentRow> content = queryFactory
			.select(Projections.constructor(
				CommentRow.class,
				c.id,
				c.rootId,
				c.parentId,
				c.depth,
				c.authorId,
				c.contentJson,
				c.status,
				c.createdAt
			))
			.from(c)
			.where(c.id.eq(postId))
			.orderBy(order)
			.offset(pageable.getOffset())
			.limit(pageable.getPageSize())
			.fetch();

		Long total = queryFactory
			.select(c.count())
			.from(c)
			.where(c.status.ne(CommentStatus.HIDDEN))
			.fetchOne();

		return new PageImpl<>(content, pageable, total == null ? 0 : total);
	}
}
