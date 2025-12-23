package com.ongi.api.community.application.query;

import com.ongi.api.community.adatper.out.persistence.CommunityPostEntity;
import com.ongi.api.community.adatper.out.persistence.CommunityPostStatsEntity;
import com.ongi.api.community.adatper.out.persistence.enums.OwnerType;
import com.ongi.api.community.adatper.out.persistence.enums.PostSortOption;
import com.ongi.api.community.adatper.out.persistence.repository.CommunityPostLikeRepository;
import com.ongi.api.community.adatper.out.persistence.repository.CommunityPostRepository;
import com.ongi.api.community.adatper.out.persistence.repository.CommunityPostStatsRepository;
import com.ongi.api.community.application.assembler.AbstractPostDetailAssembler;
import com.ongi.api.community.application.assembler.AbstractPostListAssembler;
import com.ongi.api.community.application.command.AttachmentAttachService;
import com.ongi.api.community.application.command.DocumentCodec;
import com.ongi.api.community.web.dto.PostCardItem;
import com.ongi.api.community.web.dto.PostDetailResponse;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class PostQueryService {

	private final CommunityPostRepository postRepository;

	private final CommunityPostStatsRepository statsRepository;

	private final AttachmentAttachService attachService;

	private final CommunityPostLikeRepository likeRepository;

	private final DocumentCodec documentCodec;

	private final AbstractPostListAssembler postListAssembler;

	private final AbstractPostDetailAssembler postDetailAssembler;

	@Transactional(transactionManager = "transactionManager")
	public Long create(Long userId, String title, String schema, String contentJson) {
		String text = documentCodec.extractPlainText(contentJson);
		var post = CommunityPostEntity.create(userId, title, schema, contentJson, text);
		post = postRepository.save(post);

		statsRepository.save(CommunityPostStatsEntity.create(post.getId()));

		// attachment attach
		Set<Long> attIds = documentCodec.extractAttachmentIds(contentJson);
		attachService.attachAllOrThrow(userId, OwnerType.POST, post.getId(), attIds);

		post.setCoverAttachmentId(attIds.stream().findFirst().orElse(null));

		return post.getId();
	}

	@Transactional(transactionManager = "transactionManager")
	public boolean delete(Long userId, Long postId) {
		var post = postRepository.findById(postId).orElseThrow();
		if (!post.getAuthorId().equals(userId)) throw new SecurityException("forbidden");
		post.softDelete();

		// TODO Like Comment CommentLike PostStat, Attachment 삭제?

		return true;
	}

	@Transactional(transactionManager = "transactionManager")
	public boolean like(Long userId, Long postId) {
		boolean inserted = likeRepository.insertIfNotExists(postId, userId); // native upsert 권장
		if (inserted) statsRepository.incrementLikeCount(postId, +1);
		return inserted;
	}

	@Transactional(transactionManager = "transactionManager")
	public boolean unlike(Long userId, Long postId) {
		boolean deleted = likeRepository.delete(userId, postId);
		if (deleted) statsRepository.incrementLikeCount(postId, -1);
		return deleted;
	}

	@Transactional(transactionManager = "transactionManager", readOnly = true)
	public Page<PostCardItem> getPosts(Long userId, Pageable pageable, PostSortOption sort) {
		return postListAssembler.assemble(userId, pageable, sort);
	}

	@Transactional(transactionManager = "transactionManager", readOnly = true)
	public PostDetailResponse getPost(Long userId, Long postId) {
		return postDetailAssembler.assemble(userId, postId);
	}

	@Transactional(transactionManager = "transactionManager", readOnly = true)
	public long getPostLikeCount(long postId) {
		return statsRepository.findLikeCount(postId);
	}
}
