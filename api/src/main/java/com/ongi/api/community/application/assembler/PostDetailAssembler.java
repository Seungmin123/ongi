package com.ongi.api.community.application.assembler;

import com.ongi.api.community.adatper.out.persistence.CommunityAttachmentEntity;
import com.ongi.api.community.adatper.out.persistence.QCommunityAttachmentEntity;
import com.ongi.api.community.adatper.out.persistence.QCommunityPostEntity;
import com.ongi.api.community.adatper.out.persistence.QCommunityPostStatsEntity;
import com.ongi.api.community.adatper.out.persistence.enums.AttachmentStatus;
import com.ongi.api.community.adatper.out.persistence.enums.PostStatus;
import com.ongi.api.community.adatper.out.persistence.repository.CommunityPostLikeRepository;
import com.ongi.api.community.adatper.out.user.UserInfoProvider;
import com.ongi.api.community.web.dto.PostAttachmentRow;
import com.ongi.api.community.web.dto.PostDetailRow;
import com.ongi.api.user.application.component.FileClient;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class PostDetailAssembler extends AbstractPostDetailAssembler {

	private final JPAQueryFactory queryFactory;

	public PostDetailAssembler(
		UserInfoProvider userInfoProvider,
		CommunityPostLikeRepository postLikeRepository,
		FileClient fileClient,
		JPAQueryFactory queryFactory) {
		super(userInfoProvider, postLikeRepository, fileClient);
		this.queryFactory = queryFactory;
	}

	@Override
	protected PostDetailRow fetchPostDetailRow(Long viewerId, Long postId) {
		QCommunityPostEntity p = QCommunityPostEntity.communityPostEntity;
		QCommunityPostStatsEntity s = QCommunityPostStatsEntity.communityPostStatsEntity;

		PostDetailRow row = queryFactory
			.select(Projections.constructor(
				PostDetailRow.class,
				p.id,
				p.authorId,
				p.title,
				p.contentSchema,
				p.contentJson,
				s.likeCount,
				s.commentCount,
				s.viewCount,
				p.createdAt
			))
			.from(p)
			.join(s).on(s.postId.eq(p.id))
			.where(p.id.eq(postId))
			.fetchOne();

		if (row == null) throw new IllegalStateException("Post not found");
		return row;
	}

	@Override
	protected List<PostAttachmentRow> fetchAttachments(Long postId) {
		QCommunityAttachmentEntity att = QCommunityAttachmentEntity.communityAttachmentEntity;

		var row = queryFactory
			.select(Projections.constructor(
				PostAttachmentRow.class,
				att.id, att.storageKey, att.width, att.height, att.mimeType
			))
			.from(att)
			.where(att.ownerId.eq(postId)
				.and(att.status.eq(AttachmentStatus.ATTACHED)))
			.fetch();

		if (row == null) return new ArrayList<>();
		return row;
	}
}
