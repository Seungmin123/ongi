package com.ongi.api.recipe.application.query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.ongi.api.recipe.adapter.out.persistence.RecipeAdapter;
import com.ongi.api.recipe.adapter.out.persistence.repository.RecipeBookmarkRepository;
import com.ongi.api.recipe.adapter.out.persistence.repository.RecipeLikeRepository;
import com.ongi.api.recipe.adapter.out.persistence.repository.RecipeStatsRepository;
import com.ongi.api.recipe.application.assembler.RecipeCommentAssembler;
import com.ongi.api.recipe.web.dto.CommentCreateRequest;
import com.ongi.recipe.domain.RecipeComment;
import com.ongi.recipe.domain.enums.RecipeCommentStatus;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RecipeQueryServiceTest {

    @Mock
    private JPAQueryFactory queryFactory;
    @Mock
    private RecipeCommentAssembler assembler;
    @Mock
    private RecipeAdapter recipeAdapter;
    @Mock
    private RecipeStatsRepository recipeStatsRepository;
    @Mock
    private RecipeLikeRepository recipeLikeRepository;
    @Mock
    private RecipeBookmarkRepository recipeBookmarkRepository;

    @InjectMocks
    private RecipeQueryService recipeQueryService;

    @Test
    @DisplayName("좋아요 - 처음 누르는 경우 성공 및 카운트 증가")
    void like_success() {
        // given
        long userId = 1L;
        long recipeId = 10L;
        given(recipeLikeRepository.insertIfNotExists(userId, recipeId)).willReturn(true);

        // when
        boolean result = recipeQueryService.like(userId, recipeId);

        // then
        assertThat(result).isTrue();
        verify(recipeStatsRepository).incrementLikeCount(recipeId, 1);
    }

    @Test
    @DisplayName("좋아요 - 이미 누른 경우 카운트가 증가하지 않아야 함")
    void like_already_exists() {
        // given
        long userId = 1L;
        long recipeId = 10L;
        given(recipeLikeRepository.insertIfNotExists(userId, recipeId)).willReturn(false);

        // when
        boolean result = recipeQueryService.like(userId, recipeId);

        // then
        assertThat(result).isFalse();
        verify(recipeStatsRepository, never()).incrementLikeCount(anyLong(), anyInt());
    }

    @Test
    @DisplayName("댓글 생성 - 루트 댓글 성공")
    void createRecipeComment_root_success() {
        // given
        long userId = 1L;
        long recipeId = 10L;
        // CommentCreateRequest(content, parentId)
        CommentCreateRequest req = new CommentCreateRequest("Root Comment", null);
        
        RecipeComment comment = mock(RecipeComment.class);
        given(comment.getId()).willReturn(100L);
        given(recipeAdapter.existsRecipeById(recipeId)).willReturn(true);
        given(recipeAdapter.createRootComment(userId, recipeId, req.content())).willReturn(comment);

        // when
        Long commentId = recipeQueryService.createRecipeComment(userId, recipeId, req);

        // then
        assertThat(commentId).isEqualTo(100L);
        verify(recipeStatsRepository).upsertIncCommentCount(recipeId, 1);
    }

    @Test
    @DisplayName("댓글 생성 - 대댓글 생성 시 부모가 삭제된 상태면 예외 발생")
    void createRecipeComment_reply_parent_deleted_fail() {
        // given
        long userId = 1L;
        long recipeId = 10L;
        long parentId = 50L;
        CommentCreateRequest req = new CommentCreateRequest("Reply", parentId);

        RecipeComment parent = mock(RecipeComment.class);
        given(parent.getStatus()).willReturn(RecipeCommentStatus.DELETED); // 삭제된 부모

        given(recipeAdapter.existsRecipeById(recipeId)).willReturn(true);
        given(recipeAdapter.findRecipeCommentByIdAndRecipeId(parentId, recipeId)).willReturn(Optional.of(parent));

        // when & then
        assertThatThrownBy(() -> recipeQueryService.createRecipeComment(userId, recipeId, req))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("parent deleted");
    }

    @Test
    @DisplayName("댓글 삭제 - 본인이 아닌 경우 SecurityException 발생")
    void deleteRecipeComment_forbidden() {
        // given
        long userId = 1L;
        long authorId = 2L;
        long recipeId = 10L;
        long commentId = 100L;

        RecipeComment comment = mock(RecipeComment.class);
        given(comment.getUserId()).willReturn(authorId);
        given(recipeAdapter.findRecipeCommentByIdAndRecipeId(commentId, recipeId)).willReturn(Optional.of(comment));

        // when & then
        assertThatThrownBy(() -> recipeQueryService.deleteRecipeComment(userId, recipeId, commentId))
            .isInstanceOf(SecurityException.class)
            .hasMessage("forbidden");
    }
}