package com.ongi.api.community.application.command;

import com.ongi.api.community.adatper.out.persistence.repository.CommunityPostLikeRepository;
import com.ongi.api.community.adatper.out.persistence.repository.CommunityPostRepository;
import com.ongi.api.community.adatper.out.persistence.repository.CommunityPostStatsRepository;
import com.ongi.api.community.adatper.out.persistence.enums.OwnerType;
import com.ongi.api.community.adatper.out.persistence.enums.PostStatus;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class PostCommandService {

	private final CommunityPostRepository postRepository;

	private final CommunityPostStatsRepository statsRepository;

	private final AttachmentAttachService attachService;

	private final CommunityPostLikeRepository likeRepository;

	private final DocumentCodec documentCodec;

	@Transactional(transactionManager = "transactionManager")
	public Long update(Long userId, Long postId, String title, String schema, String contentJson) {
		var post = postRepository.findById(postId).orElseThrow();
		if (!post.getAuthorId().equals(userId)) throw new SecurityException("forbidden");
		if (post.getStatus() != PostStatus.ACTIVE) throw new IllegalStateException("not active");

		String text = documentCodec.extractPlainText(contentJson);
		post.update(title, schema, contentJson, text);

		// 새 attachment attach (기존 ATTACHED는 그대로; 문서에서 빠진 첨부를 DETACH/DELETE 할지 정책 필요)
		Set<Long> attIds = documentCodec.extractAttachmentIds(contentJson);
		attachService.attachAllOrThrow(userId, OwnerType.POST, postId, attIds);

		post.setCoverAttachmentId(attIds.stream().findFirst().orElse(null));

		return post.getId();
	}


}
