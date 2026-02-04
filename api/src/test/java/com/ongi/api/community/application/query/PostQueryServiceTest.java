package com.ongi.api.community.application.query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.ongi.api.community.adatper.out.persistence.CommunityPostEntity;
import com.ongi.api.community.adatper.out.persistence.repository.CommunityPostLikeRepository;
import com.ongi.api.community.adatper.out.persistence.repository.CommunityPostRepository;
import com.ongi.api.community.adatper.out.persistence.repository.CommunityPostStatsRepository;
import com.ongi.api.community.application.assembler.AbstractPostDetailAssembler;
import com.ongi.api.community.application.assembler.AbstractPostListAssembler;
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
class PostQueryServiceTest {

    @Mock private CommunityPostRepository postRepository;
    @Mock private CommunityPostStatsRepository statsRepository;
    @Mock private AttachmentAttachService attachService;
    @Mock private CommunityPostLikeRepository likeRepository;
    @Mock private DocumentCodec documentCodec;
    @Mock private AbstractPostListAssembler postListAssembler;
    @Mock private AbstractPostDetailAssembler postDetailAssembler;

    @InjectMocks
    private PostQueryService postQueryService;

    @Test
    @DisplayName("게시글 생성 - 성공 (통계 초기화 및 첨부파일 대표 설정 포함)")
    void create_success() {
        // given
        Long userId = 1L;
        String contentJson = "{}";
        CommunityPostEntity post = mock(CommunityPostEntity.class);
        given(post.getId()).willReturn(100L);
        given(postRepository.save(any())).willReturn(post);
        given(documentCodec.extractAttachmentIds(contentJson)).willReturn(Set.of(500L));

        // when
        Long result = postQueryService.create(userId, "Title", "schema", contentJson);

        // then
        assertThat(result).isEqualTo(100L);
        verify(statsRepository).save(any());
        verify(post).setCoverAttachmentId(500L); // 첫 번째 첨부파일이 커버로 설정됨
    }

    @Test
    @DisplayName("게시글 삭제 - 성공")
    void delete_success() {
        // given
        Long userId = 1L, postId = 100L;
        CommunityPostEntity post = mock(CommunityPostEntity.class);
        given(post.getAuthorId()).willReturn(userId);
        given(postRepository.findById(postId)).willReturn(Optional.of(post));

        // when
        boolean result = postQueryService.delete(userId, postId);

        // then
        assertThat(result).isTrue();
        verify(post).softDelete();
    }

    @Test
    @DisplayName("게시글 삭제 - 타인 글이면 SecurityException")
    void delete_forbidden() {
        // given
        CommunityPostEntity post = mock(CommunityPostEntity.class);
        given(post.getAuthorId()).willReturn(2L);
        given(postRepository.findById(100L)).willReturn(Optional.of(post));

        // when & then
        assertThatThrownBy(() -> postQueryService.delete(1L, 100L))
            .isInstanceOf(SecurityException.class)
            .hasMessage("forbidden");
    }

    @Test
    @DisplayName("게시글 좋아요 - 성공 및 카운트 증가")
    void like_success() {
        // given
        given(likeRepository.insertIfNotExists(100L, 1L)).willReturn(true);

        // when
        boolean result = postQueryService.like(1L, 100L);

        // then
        assertThat(result).isTrue();
        verify(statsRepository).incrementLikeCount(100L, 1);
    }
}
