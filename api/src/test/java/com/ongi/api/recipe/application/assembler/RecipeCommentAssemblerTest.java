package com.ongi.api.recipe.application.assembler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.BDDMockito.given;

import com.ongi.api.recipe.port.UserInfoProvider;
import com.ongi.api.recipe.web.dto.CommentRow;
import com.ongi.api.recipe.web.dto.RecipeCommentItem;
import com.ongi.api.recipe.web.dto.UserSummary;
import com.ongi.recipe.domain.enums.CommentSortOption;
import com.ongi.recipe.domain.enums.RecipeCommentStatus;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
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
class RecipeCommentAssemblerTest {

    @Mock
    private UserInfoProvider userInfoProvider;

    @Mock
    private JPAQueryFactory queryFactory;

    private RecipeCommentAssembler assembler;

    @BeforeEach
    void setUp() {
        // RecipeCommentAssembler를 상속받아 fetchComments만 오버라이드한 테스트용 익명 클래스 사용
        assembler = new RecipeCommentAssembler(userInfoProvider, queryFactory) {
            @Override
            protected Page<CommentRow> fetchComments(Long recipeId, Pageable pageable, CommentSortOption sort) {
                return createMockCommentRows(pageable);
            }
        };
    }

    private Page<CommentRow> createMockCommentRows(Pageable pageable) {
        CommentRow row1 = new CommentRow(
            1L, 1L, null, 0, 10L, "정말 맛있어요!", RecipeCommentStatus.ACTIVE, LocalDateTime.now()
        );
        CommentRow row2 = new CommentRow(
            2L, 2L, null, 0, 11L, "나쁜 댓글", RecipeCommentStatus.DELETED, LocalDateTime.now()
        );
        return new PageImpl<>(List.of(row1, row2), pageable, 2);
    }

    @Test
    @DisplayName("댓글 목록 조립 - 사용자 정보 병합 및 삭제된 댓글 처리 확인")
    void assemble_success() {
        // given
        Long recipeId = 1L;
        Pageable pageable = PageRequest.of(0, 10);
        CommentSortOption sort = CommentSortOption.LATEST;

        UserSummary user10 = new UserSummary(10L, "작성자1", "url1");
        UserSummary user11 = new UserSummary(11L, "작성자2", "url2");

        given(userInfoProvider.getUsersByIds(anySet()))
            .willReturn(Map.of(10L, user10, 11L, user11));

        // when
        Page<RecipeCommentItem> result = assembler.assemble(recipeId, pageable, sort);

        // then
        assertThat(result.getContent()).hasSize(2);
        
        // 첫 번째 댓글 (ACTIVE)
        RecipeCommentItem item1 = result.getContent().get(0);
        assertThat(item1.commentId()).isEqualTo(1L);
        assertThat(item1.content()).isEqualTo("정말 맛있어요!");
        assertThat(item1.userSummary().displayName()).isEqualTo("작성자1");

        // 두 번째 댓글 (DELETED)
        RecipeCommentItem item2 = result.getContent().get(1);
        assertThat(item2.commentId()).isEqualTo(2L);
        assertThat(item2.content()).isEqualTo("삭제된 댓글입니다."); // 마스킹 확인
        assertThat(item2.userSummary().displayName()).isEqualTo("작성자2");
    }

    @Test
    @DisplayName("작성자 정보가 없는 경우에도 정상 동작해야 함")
    void assemble_with_missing_user_info() {
        // given
        Long recipeId = 1L;
        Pageable pageable = PageRequest.of(0, 10);
        
        given(userInfoProvider.getUsersByIds(anySet()))
            .willReturn(Map.of()); // 사용자 정보가 비어있음

        // when
        Page<RecipeCommentItem> result = assembler.assemble(recipeId, pageable, CommentSortOption.LATEST);

        // then
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent().get(0).userSummary()).isNull();
    }
}