package com.ongi.api.community.application.assembler;

import com.ongi.api.community.adatper.out.file.AttachmentReadClient;
import com.ongi.api.community.adatper.out.persistence.QCommunityPostEntity;
import com.ongi.api.community.adatper.out.persistence.QCommunityPostStatsEntity;
import com.ongi.api.community.adatper.out.persistence.enums.PostSortOption;
import com.ongi.api.community.adatper.out.persistence.enums.PostStatus;
import com.ongi.api.community.adatper.out.persistence.repository.CommunityPostLikeRepository;
import com.ongi.api.community.adatper.out.user.UserInfoProvider;
import com.ongi.api.community.web.dto.PostCardRow;
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
public class PostListAssembler extends AbstractPostListAssembler {

	private final JPAQueryFactory queryFactory;

	public PostListAssembler(
		UserInfoProvider userInfoProvider,
		AttachmentReadClient attachmentReadClient,
		CommunityPostLikeRepository postLikeRepository,
		JPAQueryFactory queryFactory) {
		super(userInfoProvider, attachmentReadClient, postLikeRepository);
		this.queryFactory = queryFactory;
	}

	@Override
	protected Page<PostCardRow> fetchPosts(Pageable pageable, PostSortOption sort) {
		QCommunityPostEntity p = QCommunityPostEntity.communityPostEntity;
		QCommunityPostStatsEntity s = QCommunityPostStatsEntity.communityPostStatsEntity;

		OrderSpecifier<?>[] order = toOrderSpecifiers(sort, p, s);

		List<PostCardRow> content = queryFactory
			.select(Projections.constructor(
				PostCardRow.class,
				p.id,
				p.authorId,
				p.title,
				p.contentText,
				p.coverAttachmentId,
				s.likeCount,
				s.commentCount,
				s.viewCount,
				p.createdAt
			))
			.from(p)
			.join(s).on(s.postId.eq(p.id)) // stats는 1:1이라 join 허용
			.where(p.status.ne(PostStatus.HIDDEN))
			.orderBy(order)
			.offset(pageable.getOffset())
			.limit(pageable.getPageSize())
			.fetch();

		Long total = queryFactory
			.select(p.count())
			.from(p)
			.where(p.status.ne(PostStatus.HIDDEN))
			.fetchOne();

		return new PageImpl<>(content, pageable, total == null ? 0 : total);
	}

	private OrderSpecifier<?>[] toOrderSpecifiers(PostSortOption sort, QCommunityPostEntity post, QCommunityPostStatsEntity postStats) {
		return switch (sort) {
			case CREATED_ASC -> new OrderSpecifier[]{
				new OrderSpecifier<>(Order.ASC, post.createdAt),
				new OrderSpecifier<>(Order.ASC, post.id)
			};
			case CREATED_DESC -> new OrderSpecifier[]{
				new OrderSpecifier<>(Order.DESC, post.createdAt),
				new OrderSpecifier<>(Order.DESC, post.id)
			};
			case ID_ASC -> new OrderSpecifier[]{
				new OrderSpecifier<>(Order.ASC, post.id)
			};
			case ID_DESC -> new OrderSpecifier[]{
				new OrderSpecifier<>(Order.DESC, post.id)
			};
			case VIEWS_ASC -> new OrderSpecifier[]{
				new OrderSpecifier<>(Order.ASC, postStats.viewCount),
			};
			case VIEWS_DESC -> new OrderSpecifier[]{
				new OrderSpecifier<>(Order.DESC, postStats.viewCount),
			};
		};
	}
}
