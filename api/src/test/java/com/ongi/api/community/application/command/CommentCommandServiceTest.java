package com.ongi.api.community.application.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.ongi.api.community.adatper.out.persistence.CommunityCommentEntity;
import com.ongi.api.community.adatper.out.persistence.enums.OwnerType;
import com.ongi.api.community.adatper.out.persistence.repository.CommunityCommentRepository;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CommentCommandServiceTest {

    @Mock private CommunityCommentRepository commentRepository;
    @Mock private AttachmentAttachService attachService;
    @Mock private DocumentCodec documentCodec;

    @InjectMocks
    private CommentCommandService commentCommandService;

    @Test
    @DisplayName("댓글 수정 - 성공")
    void update_success() {
        // given
        Long userId = 1L;
        Long postId = 10L;
        Long commentId = 100L;
        String contentJson = "{}";

        CommunityCommentEntity comment = mock(CommunityCommentEntity.class);
        given(comment.getAuthorId()).willReturn(userId);
        given(comment.getPostId()).willReturn(postId);
        given(commentRepository.findById(commentId)).willReturn(Optional.of(comment));
        
        given(documentCodec.extractPlainText(contentJson)).willReturn("Text");
        given(documentCodec.extractAttachmentIds(contentJson)).willReturn(Set.of());

        // when
        boolean result = commentCommandService.update(userId, postId, commentId, "schema", contentJson);

        // then
        assertThat(result).isTrue();
        verify(comment).update(eq("schema"), eq(contentJson), eq("Text"));
        verify(attachService).attachAllOrThrow(eq(userId), eq(OwnerType.COMMENT), eq(commentId), any());
    }

    @Test
    @DisplayName("댓글 수정 - 게시글 ID가 매칭되지 않으면 실패")
    void update_mismatch_post() {
        // given
        CommunityCommentEntity comment = mock(CommunityCommentEntity.class);
        given(comment.getPostId()).willReturn(99L); 
        given(commentRepository.findById(100L)).willReturn(Optional.of(comment));

        // when & then
        assertThatThrownBy(() -> commentCommandService.update(1L, 10L, 100L, "S", "{}"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("mismatch");
    }
}