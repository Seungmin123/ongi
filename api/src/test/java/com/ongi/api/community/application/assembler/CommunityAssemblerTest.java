package com.ongi.api.community.application.assembler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import com.ongi.api.community.adatper.out.file.AttachmentReadClient;
import com.ongi.api.community.adatper.out.persistence.enums.CommentSortOption;
import com.ongi.api.community.adatper.out.persistence.enums.CommentStatus;
import com.ongi.api.community.adatper.out.persistence.enums.PostSortOption;
import com.ongi.api.community.adatper.out.persistence.repository.CommunityCommentLikeRepository;
import com.ongi.api.community.adatper.out.persistence.repository.CommunityPostLikeRepository;
import com.ongi.api.community.adatper.out.user.UserInfoProvider;
import com.ongi.api.community.adatper.out.user.UserSummary;
import com.ongi.api.community.web.dto.AttachmentDto;
import com.ongi.api.community.web.dto.CommentItem;
import com.ongi.api.community.web.dto.CommentRow;
import com.ongi.api.community.web.dto.PostAttachmentRow;
import com.ongi.api.community.web.dto.PostCardItem;
import com.ongi.api.community.web.dto.PostCardRow;
import com.ongi.api.community.web.dto.PostDetailResponse;
import com.ongi.api.community.web.dto.PostDetailRow;
import com.ongi.api.user.application.component.FileClient;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class CommunityAssemblerTest {

    @Mock private UserInfoProvider userInfoProvider;
    @Mock private CommunityCommentLikeRepository commentLikeRepository;
    @Mock private CommunityPostLikeRepository postLikeRepository;
    @Mock private AttachmentReadClient attachmentReadClient;
    @Mock private FileClient fileClient;
    @Mock private JPAQueryFactory queryFactory;

    private CommentListAssembler commentListAssembler;
    private PostListAssembler postListAssembler;
    private PostDetailAssembler postDetailAssembler;

    @BeforeEach
    void setUp() {
        commentListAssembler = new CommentListAssembler(userInfoProvider, commentLikeRepository, queryFactory) {
            @Override
            protected Page<CommentRow> fetchComments(Long postId, Pageable pageable, CommentSortOption sort) {
                CommentRow r1 = new CommentRow(10L, 10L, null, 0, 1L, "Content 1", CommentStatus.ACTIVE, LocalDateTime.now());
                CommentRow r2 = new CommentRow(11L, 10L, null, 0, 2L, "Hidden", CommentStatus.DELETED, LocalDateTime.now());
                return new PageImpl<>(List.of(r1, r2), pageable, 2);
            }
        };

        postListAssembler = new PostListAssembler(userInfoProvider, attachmentReadClient, postLikeRepository, queryFactory) {
            @Override
            protected Page<PostCardRow> fetchPosts(Pageable pageable, PostSortOption sort) {
                PostCardRow r1 = new PostCardRow(100L, 1L, "Title", "Text", 500L, 10, 5, 100, LocalDateTime.now());
                return new PageImpl<>(List.of(r1), pageable, 1);
            }
        };

        postDetailAssembler = new PostDetailAssembler(userInfoProvider, postLikeRepository, fileClient, queryFactory) {
            @Override
            protected PostDetailRow fetchPostDetailRow(Long userId, Long postId) {
                if (postId == 999L) throw new IllegalStateException("Post not found");
                return new PostDetailRow(postId, 1L, "Detail Title", 1, "{}", 10, 5, 100, LocalDateTime.now());
            }
            @Override
            protected List<PostAttachmentRow> fetchAttachments(Long postId) {
                return List.of(new PostAttachmentRow(500L, "key/url.jpg", 1080, 1920, "image/jpeg"));
            }
        };
    }

    @Test
    @DisplayName("댓글 목록 조립 - 삭제된 댓글 마스킹 및 좋아요 여부 확인")
    void assemble_comments_success() {
        // given
        Long userId = 1L;
        UserSummary user = new UserSummary(1L, "작성자", "url");
        given(userInfoProvider.getUserSummaries(anySet())).willReturn(Map.of(1L, user));
        given(commentLikeRepository.findLikedCommentIds(eq(userId), anySet())).willReturn(Set.of(10L));

        // when
        Page<CommentItem> result = commentListAssembler.assemble(userId, 100L, PageRequest.of(0, 10), CommentSortOption.CREATED_DESC);

        // then
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent().get(0).liked()).isTrue();
        assertThat(result.getContent().get(0).content()).isEqualTo("Content 1");
        
        assertThat(result.getContent().get(1).content()).isEqualTo("삭제된 댓글입니다.");
        assertThat(result.getContent().get(1).liked()).isFalse();
    }

    @Test
    @DisplayName("게시글 목록 조립 - 작성자 및 커버 이미지 병합 확인")
    void assemble_posts_success() {
        // given
        UserSummary user = new UserSummary(1L, "작성자", "url");
        AttachmentDto cover = new AttachmentDto(500L, "signed-url", 100, 100, "image/jpeg");
        
        given(userInfoProvider.getUserSummaries(anySet())).willReturn(Map.of(1L, user));
        given(attachmentReadClient.getAttachmentsByIds(anySet())).willReturn(Map.of(500L, cover));

        // when
        Page<PostCardItem> result = postListAssembler.assemble(null, PageRequest.of(0, 10), PostSortOption.CREATED_DESC);

        // then
        assertThat(result.getContent()).hasSize(1);
        PostCardItem item = result.getContent().get(0);
        assertThat(item.author().displayName()).isEqualTo("작성자");
        assertThat(item.cover().url()).isEqualTo("signed-url");
    }

    @Test
    @DisplayName("게시글 상세 조립 - 첨부파일 Signed URL 생성 확인")
    void assemble_post_detail_success() {
        // given
        UserSummary user = new UserSummary(1L, "작성자", "url");
        given(userInfoProvider.getUserSummaries(anySet())).willReturn(Map.of(1L, user));
        given(fileClient.generateSignedUrl(anyString(), anyInt())).willReturn("https://signed-url.com/1.jpg");

        // when
        PostDetailResponse result = postDetailAssembler.assemble(1L, 100L);

        // then
        assertThat(result.title()).isEqualTo("Detail Title");
        assertThat(result.attachments()).hasSize(1);
        assertThat(result.attachments().get(0).url()).isEqualTo("https://signed-url.com/1.jpg");
    }

    @Test
    @DisplayName("게시글 상세 조립 - 존재하지 않는 게시글일 경우 예외 발생")
    void assemble_post_detail_fail_not_found() {
        // when & then
        assertThatThrownBy(() -> postDetailAssembler.assemble(1L, 999L))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("Post not found");
    }
}