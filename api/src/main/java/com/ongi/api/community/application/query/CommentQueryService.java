package com.ongi.api.community.application.query;

import com.ongi.api.community.adatper.out.persistence.CommunityCommentEntity;
import com.ongi.api.community.adatper.out.persistence.enums.CommentSortOption;
import com.ongi.api.community.adatper.out.persistence.enums.CommentStatus;
import com.ongi.api.community.adatper.out.persistence.enums.OwnerType;
import com.ongi.api.community.adatper.out.persistence.enums.PostStatus;
import com.ongi.api.community.adatper.out.persistence.repository.CommunityCommentLikeRepository;
import com.ongi.api.community.adatper.out.persistence.repository.CommunityCommentRepository;
import com.ongi.api.community.adatper.out.persistence.repository.CommunityPostRepository;
import com.ongi.api.community.adatper.out.persistence.repository.CommunityPostStatsRepository;
import com.ongi.api.community.application.assembler.AbstractCommentListAssembler;
import com.ongi.api.community.application.command.AttachmentAttachService;
import com.ongi.api.community.application.command.DocumentCodec;
import com.ongi.api.community.web.dto.CommentItem;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class CommentQueryService {

	private final CommunityPostRepository postRepository;

	private final CommunityCommentRepository commentRepository;

	private final CommunityPostStatsRepository statsRepository;

	private final AttachmentAttachService attachService;

	private final CommunityCommentLikeRepository likeRepository;

	private final DocumentCodec documentCodec;

	private final AbstractCommentListAssembler assembler;

	@Transactional(transactionManager = "transactionManager")
	public Long create(Long userId, Long postId, Long parentId, String schema, String contentJson) {
		if (!postRepository.existsByIdAndStatus(postId, PostStatus.ACTIVE)) {
			throw new IllegalArgumentException("post not found");
		}

		String text = documentCodec.extractPlainText(contentJson);
		CommunityCommentEntity comment;

		if (parentId == null) {
			comment = CommunityCommentEntity.createRoot(postId, userId, schema, contentJson, text);
			comment = commentRepository.save(comment);
			comment.attachRootId(comment.getId());
		} else {
			var parent = commentRepository.findById(parentId).orElseThrow();
			if (!parent.getPostId().equals(postId)) throw new IllegalArgumentException("parent mismatch");
			if (parent.getStatus() != CommentStatus.ACTIVE) throw new IllegalStateException("parent deleted");

			comment = CommunityCommentEntity.createReply(
				postId,
				userId,
				parentId,
				parent.getRootId(),
				parent.getDepth() + 1,
				schema, contentJson, text
			);
			comment = commentRepository.save(comment);
		}

		statsRepository.incrementCommentCount(postId, +1);

		// attachment attach (댓글도 가능)
		Set<Long> attIds = documentCodec.extractAttachmentIds(contentJson);
		attachService.attachAllOrThrow(userId, OwnerType.COMMENT, comment.getId(), attIds);

		return comment.getId();
	}

	@Transactional(transactionManager = "transactionManager")
	public boolean delete(Long userId, Long postId, Long commentId) {
		var comment = commentRepository.findById(commentId).orElseThrow(() -> new IllegalArgumentException("Post not found"));
		if (!comment.getPostId().equals(postId)) throw new IllegalArgumentException("mismatch");
		if (!comment.getAuthorId().equals(userId)) throw new SecurityException("forbidden");

		boolean deleted = comment.softDelete();
		if (deleted) statsRepository.incrementCommentCount(postId, -1);

		// TODO Like 등 삭제?

		return deleted;
	}

	@Transactional(transactionManager = "transactionManager", readOnly = true)
	public Page<CommentItem> getComments(Long userId, Long postId, Pageable pageable, CommentSortOption sort) {
		return assembler.assemble(userId, postId, pageable, sort);
	}

	@Transactional(transactionManager = "transactionManager")
	public boolean like(Long userId, Long postId, Long commentId) {
		var comment = commentRepository.findById(commentId).orElseThrow();
		if (!comment.getPostId().equals(postId)) throw new IllegalArgumentException("mismatch");
		if (!comment.getAuthorId().equals(userId)) throw new SecurityException("forbidden");

		boolean inserted = likeRepository.insertIfNotExists(userId, commentId);
		if (inserted) commentRepository.incrementLikeCount(postId, commentId, +1);
		return inserted;
	}

	@Transactional(transactionManager = "transactionManager")
	public boolean unlike(Long userId, Long postId, Long commentId) {
		var comment = commentRepository.findById(commentId).orElseThrow();
		if (!comment.getPostId().equals(postId)) throw new IllegalArgumentException("mismatch");
		if (!comment.getAuthorId().equals(userId)) throw new SecurityException("forbidden");

		boolean delete = likeRepository.delete(userId, commentId);
		if (delete) commentRepository.incrementLikeCount(postId, commentId, -1);
		return delete;
	}

	@Transactional(transactionManager = "transactionManager", readOnly = true)
	public long getCommentLikeCount(long postId, long commentId) {
		return commentRepository.findLikeCount(postId, commentId);
	}

}
