package com.ongi.api.community.application.assembler;

import com.ongi.api.community.adatper.out.persistence.enums.CommentSortOption;
import com.ongi.api.community.adatper.out.persistence.enums.CommentStatus;
import com.ongi.api.community.adatper.out.persistence.repository.CommunityCommentLikeRepository;
import com.ongi.api.community.adatper.out.user.UserInfoProvider;
import com.ongi.api.community.adatper.out.user.UserSummary;
import com.ongi.api.community.web.dto.CommentItem;
import com.ongi.api.community.web.dto.CommentRow;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

// MSA 전환 고려 템플릿 메소드 패턴 적용
@RequiredArgsConstructor
public abstract class AbstractCommentListAssembler {

	private final UserInfoProvider userInfoProvider;

	private final CommunityCommentLikeRepository commentLikeRepository;

	public final Page<CommentItem> assemble(Long userId, Long postId, Pageable pageable, CommentSortOption sort) {
		var page = fetchComments(postId, pageable, sort);

		var authorIds = page.getContent().stream()
			.map(CommentRow::userId)
			.filter(Objects::nonNull)
			.collect(Collectors.toSet());

		var commentIds = page.getContent().stream()
			.map(CommentRow::commentId)
			.collect(Collectors.toSet());

		var users = userInfoProvider.getUserSummaries(authorIds);

		var likedCommentIds = (userId == null) ? Set.of()
			: commentLikeRepository.findLikedCommentIds(userId, commentIds);

		var items = page.getContent().stream()
			.map(r -> new CommentItem(
					r.commentId(),
					r.rootId(),
					r.parentId(),
					r.depth(),
					r.status() == CommentStatus.DELETED ? "삭제된 댓글입니다." : r.content(),
					users.get(r.userId()),
					r.createdAt(),
					userId != null && likedCommentIds.contains(r.commentId())
				))
			.toList();

		return new PageImpl<>(items, pageable, page.getTotalElements());
	}

	protected abstract Page<CommentRow> fetchComments(Long postId, Pageable pageable, CommentSortOption sort);

}
