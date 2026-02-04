package com.ongi.api.community.application.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.ongi.api.community.adatper.out.persistence.CommunityPostEntity;
import com.ongi.api.community.adatper.out.persistence.enums.OwnerType;
import com.ongi.api.community.adatper.out.persistence.enums.PostStatus;
import com.ongi.api.community.adatper.out.persistence.repository.CommunityPostRepository;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PostCommandServiceTest {

    @Mock private CommunityPostRepository postRepository;
    @Mock private AttachmentAttachService attachService;
    @Mock private DocumentCodec documentCodec;

    @InjectMocks
    private PostCommandService postCommandService;

    @Test
    @DisplayName("게시글 수정 - 성공")
    void update_success() {
        // given
        Long userId = 1L;
        Long postId = 10L;
        String contentJson = "{}";
        
        CommunityPostEntity post = mock(CommunityPostEntity.class);
        given(post.getAuthorId()).willReturn(userId);
        given(post.getStatus()).willReturn(PostStatus.ACTIVE);
        given(post.getId()).willReturn(postId);
        given(postRepository.findById(postId)).willReturn(Optional.of(post));
        
        given(documentCodec.extractPlainText(contentJson)).willReturn("Plain Text");
        given(documentCodec.extractAttachmentIds(contentJson)).willReturn(Set.of(500L));

        // when
        Long result = postCommandService.update(userId, postId, "New Title", "schema", contentJson);

        // then
        assertThat(result).isEqualTo(postId);
        verify(post).update(eq("New Title"), eq("schema"), eq(contentJson), eq("Plain Text"));
        verify(attachService).attachAllOrThrow(eq(userId), eq(OwnerType.POST), eq(postId), any());
        verify(post).setCoverAttachmentId(500L);
    }

    @Test
    @DisplayName("게시글 수정 - 작성자가 아니면 실패")
    void update_forbidden() {
        // given
        CommunityPostEntity post = mock(CommunityPostEntity.class);
        given(post.getAuthorId()).willReturn(2L); 
        given(postRepository.findById(10L)).willReturn(Optional.of(post));

        // when & then
        assertThatThrownBy(() -> postCommandService.update(1L, 10L, "T", "S", "{}"))
            .isInstanceOf(SecurityException.class)
            .hasMessage("forbidden");
    }
}