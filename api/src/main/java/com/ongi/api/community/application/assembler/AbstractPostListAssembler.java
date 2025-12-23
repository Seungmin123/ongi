package com.ongi.api.community.application.assembler;

import com.ongi.api.community.adatper.out.file.AttachmentReadClient;
import com.ongi.api.community.adatper.out.persistence.enums.PostSortOption;
import com.ongi.api.community.adatper.out.persistence.repository.CommunityPostLikeRepository;
import com.ongi.api.community.adatper.out.user.UserInfoProvider;
import com.ongi.api.community.adatper.out.user.UserSummary;
import com.ongi.api.community.web.dto.AttachmentDto;
import com.ongi.api.community.web.dto.PostCardItem;
import com.ongi.api.community.web.dto.PostCardRow;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

// MSA 전환 고려 템플릿 메소드 패턴 적용
@RequiredArgsConstructor
public abstract class AbstractPostListAssembler {

	private final UserInfoProvider userInfoProvider;

	private final AttachmentReadClient attachmentReadClient;

	private final CommunityPostLikeRepository postLikeRepository;

	public final Page<PostCardItem> assemble(Long userId, Pageable pageable, PostSortOption sort) {
		var page = fetchPosts(pageable, sort);

		var authorIds = page.getContent().stream()
			.map(PostCardRow::authorId)
			.filter(Objects::nonNull)
			.collect(Collectors.toSet());

		var coverIds = page.getContent().stream()
			.map(PostCardRow::coverAttachmentId)
			.filter(Objects::nonNull)
			.collect(Collectors.toSet());

		var postIds = page.getContent().stream()
			.map(PostCardRow::postId)
			.collect(Collectors.toSet());

		var users = userInfoProvider.getUserSummaries(authorIds);

		var covers = attachmentReadClient.getAttachmentsByIds(coverIds);

		var likedPostIds = (userId == null) ? Set.of()
			: postLikeRepository.findLikedPostIds(userId, postIds);

		var items = page.getContent().stream()
			.map(r -> new PostCardItem(
				r.postId(),
				r.title(),
				r.contentText(),
				users.get(r.authorId()),
				r.coverAttachmentId() == null ? null : covers.get(r.coverAttachmentId()),
				r.likeCount(),
				r.commentCount(),
				r.viewCount(),
				r.createdAt(),
				userId != null && likedPostIds.contains(r.postId())
			))
			.toList();

		return new PageImpl<>(items, pageable, page.getTotalElements());
	}

	protected abstract Page<PostCardRow> fetchPosts(Pageable pageable, PostSortOption sort);
}
