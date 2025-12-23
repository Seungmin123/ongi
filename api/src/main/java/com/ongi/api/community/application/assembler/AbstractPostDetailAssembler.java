package com.ongi.api.community.application.assembler;

import com.ongi.api.community.adatper.out.persistence.repository.CommunityPostLikeRepository;
import com.ongi.api.community.adatper.out.user.UserInfoProvider;
import com.ongi.api.community.web.dto.AttachmentDto;
import com.ongi.api.community.web.dto.PostAttachmentRow;
import com.ongi.api.community.web.dto.PostDetailResponse;
import com.ongi.api.community.web.dto.PostDetailRow;
import com.ongi.api.user.application.component.FileClient;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;

// MSA 전환 고려 템플릿 메소드 패턴 적용
@RequiredArgsConstructor
public abstract class AbstractPostDetailAssembler {

	private final UserInfoProvider userInfoProvider;

	private final CommunityPostLikeRepository postLikeRepository;

	private final FileClient fileClient;

	public final PostDetailResponse assemble(Long userId, Long postId) {
		var row = fetchPostDetailRow(userId, postId);

		var atts = fetchAttachments(postId);

		var authorIds = userInfoProvider.getUserSummaries(Set.of(row.authorId()));
		var author = authorIds.get(row.authorId());

		var likedPostIds = (userId == null) ? Set.of()
			: postLikeRepository.findLikedPostIds(userId, Set.of(postId));

		var attachmentDtos = atts.stream()
			.map(a -> new AttachmentDto(
				a.id(),
				fileClient.generateSignedUrl(a.url(), 10),
				a.width(),
				a.height(),
				a.mimType()))
			.toList();

		return new PostDetailResponse(
			row.postId(),
			row.title(),
			row.contentSchema(),
			row.contentJson(),
			author,
			attachmentDtos,
			row.likeCount(),
			row.commentCount(),
			row.viewCount(),
			row.createdAt(),
			userId != null && likedPostIds.contains(postId)
		);
	}

	protected abstract PostDetailRow fetchPostDetailRow(Long userId, Long postId);
	protected abstract List<PostAttachmentRow> fetchAttachments(Long postId);
}
