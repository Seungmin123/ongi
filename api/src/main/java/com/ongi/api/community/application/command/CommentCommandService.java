package com.ongi.api.community.application.command;

import com.ongi.api.community.adatper.out.persistence.repository.CommunityCommentRepository;
import com.ongi.api.community.adatper.out.persistence.enums.OwnerType;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class CommentCommandService {

	private final CommunityCommentRepository commentRepository;

	private final AttachmentAttachService attachService;

	private final DocumentCodec documentCodec;

	@Transactional(transactionManager = "transactionManager")
	public boolean update(Long userId, Long postId, Long commentId, String schema, String contentJson) {
		var comment = commentRepository.findById(commentId).orElseThrow();
		if (!comment.getPostId().equals(postId)) throw new IllegalArgumentException("mismatch");
		if (!comment.getAuthorId().equals(userId)) throw new SecurityException("forbidden");

		String text = documentCodec.extractPlainText(contentJson);
		comment.update(schema, contentJson, text);

		Set<Long> attIds = documentCodec.extractAttachmentIds(contentJson);
		attachService.attachAllOrThrow(userId, OwnerType.COMMENT, commentId, attIds);

		return true;
	}

}
