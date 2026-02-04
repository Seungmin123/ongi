package com.ongi.api.community.application.query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.ongi.api.community.adatper.out.persistence.CommunityCommentEntity;
import com.ongi.api.community.adatper.out.persistence.enums.CommentStatus;
import com.ongi.api.community.adatper.out.persistence.enums.PostStatus;
import com.ongi.api.community.adatper.out.persistence.repository.CommunityCommentLikeRepository;
import com.ongi.api.community.adatper.out.persistence.repository.CommunityCommentRepository;
import com.ongi.api.community.adatper.out.persistence.repository.CommunityPostRepository;
import com.ongi.api.community.adatper.out.persistence.repository.CommunityPostStatsRepository;
import com.ongi.api.community.application.assembler.AbstractCommentListAssembler;
import com.ongi.api.community.application.command.AttachmentAttachService;
import com.ongi.api.community.application.command.DocumentCodec;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CommentQueryServiceTest {

    @Mock private CommunityPostRepository postRepository;
    @Mock private CommunityCommentRepository commentRepository;
    @Mock private CommunityPostStatsRepository statsRepository;
    @Mock private AttachmentAttachService attachService;
    @Mock private CommunityCommentLikeRepository likeRepository;
    @Mock private DocumentCodec documentCodec;
    @Mock private AbstractCommentListAssembler assembler;

    @InjectMocks
    private CommentQueryService commentQueryService;

    @Test
    @DisplayName("댓글 생성 - 루트 댓글 성공")
    void create_root_success() {
        // given
        Long userId = 1L, postId = 10L;
        given(postRepository.existsByIdAndStatus(postId, PostStatus.ACTIVE)).willReturn(true);
        given(documentCodec.extractPlainText(any())).willReturn("text");
        
        CommunityCommentEntity comment = mock(CommunityCommentEntity.class);
        given(comment.getId()).willReturn(100L);
        given(commentRepository.save(any())).willReturn(comment);

        // when
        Long result = commentQueryService.create(userId, postId, null, "schema", "{}");

        // then
        assertThat(result).isEqualTo(100L);
        verify(statsRepository).incrementCommentCount(postId, 1);
        verify(comment).attachRootId(100L);
    }

    @Test
    @DisplayName("댓글 생성 - 게시글이 없으면 실패")
    void create_fail_post_not_found() {
        given(postRepository.existsByIdAndStatus(anyLong(), any())).willReturn(false);

        assertThatThrownBy(() -> commentQueryService.create(1L, 10L, null, "S", "{}"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("post not found");
    }

    @Test
    @DisplayName("대댓글 생성 - 부모 댓글이 다른 게시글에 있으면 실패")
    void create_reply_mismatch_post() {
        // given
        Long postId = 10L, parentId = 50L;
        given(postRepository.existsByIdAndStatus(postId, PostStatus.ACTIVE)).willReturn(true);
        
        CommunityCommentEntity parent = mock(CommunityCommentEntity.class);
        given(parent.getPostId()).willReturn(99L); // 다른 글의 댓글
        given(commentRepository.findById(parentId)).willReturn(Optional.of(parent));

        // when & then
        assertThatThrownBy(() -> commentQueryService.create(1L, postId, parentId, "S", "{}"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("parent mismatch");
    }

    @Test
    @DisplayName("대댓글 생성 - 부모 댓글이 삭제된 상태면 실패")
    void create_reply_parent_deleted() {
        // given
        Long postId = 10L, parentId = 50L;
        given(postRepository.existsByIdAndStatus(postId, PostStatus.ACTIVE)).willReturn(true);
        
        CommunityCommentEntity parent = mock(CommunityCommentEntity.class);
        given(parent.getPostId()).willReturn(postId);
        given(parent.getStatus()).willReturn(CommentStatus.DELETED);
        given(commentRepository.findById(parentId)).willReturn(Optional.of(parent));

        // when & then
        assertThatThrownBy(() -> commentQueryService.create(1L, postId, parentId, "S", "{}"))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("parent deleted");
    }

    @Test
    @DisplayName("댓글 삭제 - 성공")
    void delete_success() {
        // given
        Long userId = 1L, postId = 10L, commentId = 100L;
        CommunityCommentEntity comment = mock(CommunityCommentEntity.class);
        given(comment.getPostId()).willReturn(postId);
        given(comment.getAuthorId()).willReturn(userId);
        given(comment.softDelete()).willReturn(true);
        given(commentRepository.findById(commentId)).willReturn(Optional.of(comment));

        // when
        boolean result = commentQueryService.delete(userId, postId, commentId);

        // then
        assertThat(result).isTrue();
        verify(statsRepository).incrementCommentCount(postId, -1);
    }

    @Test
    @DisplayName("댓글 삭제 - 본인이 아니면 실패")
    void delete_forbidden() {
        // given
        CommunityCommentEntity comment = mock(CommunityCommentEntity.class);
        given(comment.getPostId()).willReturn(10L);
        given(comment.getAuthorId()).willReturn(2L); // 작성자 2번
        given(commentRepository.findById(100L)).willReturn(Optional.of(comment));

        // when & then
        assertThatThrownBy(() -> commentQueryService.delete(1L, 10L, 100L))
            .isInstanceOf(SecurityException.class)
            .hasMessage("forbidden");
    }

    @Test
    @DisplayName("댓글 좋아요 - 성공")
    void like_success() {
        // given
        Long userId = 1L, postId = 10L, commentId = 100L;
        CommunityCommentEntity comment = mock(CommunityCommentEntity.class);
        given(comment.getPostId()).willReturn(postId);
        given(comment.getAuthorId()).willReturn(userId); // 작성자만 좋아요 가능 정책? (현재 코드 기준)
        given(commentRepository.findById(commentId)).willReturn(Optional.of(comment));
        given(likeRepository.insertIfNotExists(userId, commentId)).willReturn(true);

        // when
        boolean result = commentQueryService.like(userId, postId, commentId);

        // then
        assertThat(result).isTrue();
        verify(commentRepository).incrementLikeCount(postId, commentId, 1);
    }

    @Test
    @DisplayName("댓글 좋아요 - 이미 한 경우 카운트 증가 안함")
    void like_already_done() {
        // given
        CommunityCommentEntity comment = mock(CommunityCommentEntity.class);
        given(comment.getPostId()).willReturn(10L);
        given(comment.getAuthorId()).willReturn(1L);
        given(commentRepository.findById(100L)).willReturn(Optional.of(comment));
        given(likeRepository.insertIfNotExists(1L, 100L)).willReturn(false);

        // when
        boolean result = commentQueryService.like(1L, 10L, 100L);

        // then
        assertThat(result).isFalse();
        verify(commentRepository, never()).incrementLikeCount(anyLong(), anyLong(), anyLong());
    }
}
