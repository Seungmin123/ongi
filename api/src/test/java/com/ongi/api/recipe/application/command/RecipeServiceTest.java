package com.ongi.api.recipe.application.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.ongi.api.common.web.dto.ApiResponse;
import com.ongi.api.ingredients.adapter.out.persistence.IngredientAdapter;
import com.ongi.api.recipe.adapter.out.cache.RecipeCacheReader;
import com.ongi.api.recipe.adapter.out.cache.TrendingReicpeStore;
import com.ongi.api.recipe.adapter.out.persistence.RecipeAdapter;
import com.ongi.api.recipe.messaging.consumer.RecipeCacheVersionResolver;
import com.ongi.api.recipe.web.dto.CursorPageRequest;
import com.ongi.api.recipe.web.dto.RecipeCacheValue;
import com.ongi.api.recipe.web.dto.RecipeCardResponse;
import com.ongi.api.recipe.web.dto.RecipeDetailBaseResponse;
import com.ongi.api.recipe.web.dto.RecipeIngredientCacheValue;
import com.ongi.api.recipe.web.dto.RecipeStepsCacheValue;
import com.ongi.api.recipe.web.dto.RecipeUpsertRequest;
import com.ongi.recipe.domain.Recipe;
import com.ongi.recipe.domain.RecipeStats;
import com.ongi.recipe.domain.enums.RecipeCategoryEnum;
import com.ongi.recipe.domain.enums.RecipeDifficultyEnum;
import com.ongi.recipe.domain.search.RecipeSearchCondition;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RecipeServiceTest {

    @Mock
    private RecipeCacheReader recipeCacheReader;
    @Mock
    private RecipeCacheVersionResolver recipeCacheVersionResolver;
    @Mock
    private RecipeAdapter recipeAdapter;
    @Mock
    private IngredientAdapter ingredientAdapter;
    @Mock
    private TrendingReicpeStore trendingReicpeStore;

    @InjectMocks
    private RecipeService recipeService;

    @Test
    @DisplayName("레시피 검색 - 성공 및 DTO 변환 확인")
    void search_success() {
        // given
        CursorPageRequest pageReq = new CursorPageRequest(null, 10, "LATEST");
        RecipeSearchCondition condition = mock(RecipeSearchCondition.class);
        
        Recipe recipe = mock(Recipe.class);
        given(recipe.getId()).willReturn(1L);
        given(recipe.getTitle()).willReturn("Test Recipe");
        given(recipe.getCookingTimeMin()).willReturn(45);
        given(recipe.getCategory()).willReturn(RecipeCategoryEnum.SOUP);
        given(recipe.getDifficulty()).willReturn(RecipeDifficultyEnum.MEDIUM);

        RecipeStats stats = mock(RecipeStats.class);
        given(stats.getRecipeId()).willReturn(1L);
        given(stats.getLikeCount()).willReturn(10L);

        given(recipeAdapter.search(any(), any(), anyInt(), any())).willReturn(List.of(recipe));
        given(recipeAdapter.findRecipeStatsByRecipeIds(anyList())).willReturn(List.of(stats));

        // when
        ApiResponse<List<RecipeCardResponse>> result = recipeService.search(pageReq, condition);

        // then
        assertThat(result.data()).hasSize(1);
        RecipeCardResponse card = result.data().get(0);
        assertThat(card.title()).isEqualTo("Test Recipe");
        assertThat(card.cookTimeText()).isEqualTo("45분");
        assertThat(card.likeCount()).isEqualTo(10L);
    }

    @Test
    @DisplayName("레시피 상세 조회 - 캐시 및 통계 결합 확인")
    void getRecipeDetail_success() {
        // given
        Long recipeId = 1L;
        given(recipeCacheVersionResolver.getOrInit(recipeId)).willReturn(1);
        
        RecipeCacheValue cacheValue = mock(RecipeCacheValue.class);
        given(cacheValue.title()).willReturn("Cached Title");
        given(cacheValue.cookingTimeMin()).willReturn(70);
        given(cacheValue.category()).willReturn(RecipeCategoryEnum.SOUP);
        
        given(recipeCacheReader.getRecipeById(eq(recipeId), anyInt())).willReturn(cacheValue);
        given(recipeCacheReader.getRecipeIngredients(eq(recipeId), anyInt())).willReturn(mock(RecipeIngredientCacheValue.class));
        given(recipeCacheReader.getRecipeSteps(eq(recipeId), anyInt())).willReturn(mock(RecipeStepsCacheValue.class));

        RecipeStats stats = mock(RecipeStats.class);
        given(stats.getLikeCount()).willReturn(50L);
        given(recipeAdapter.findRecipeStatsByRecipeId(recipeId)).willReturn(Optional.of(stats));

        // when
        RecipeDetailBaseResponse result = recipeService.getRecipeDetail(recipeId);

        // then
        assertThat(result.title()).isEqualTo("Cached Title");
        assertThat(result.cookTimeText()).isEqualTo("1시간 10분"); 
        assertThat(result.likeCount()).isEqualTo(50L);
    }

    @Test
    @DisplayName("레시피 생성 - 기본 정보 및 통계 초기화 확인")
    void createRecipe_success() {
        // given
        Long userId = 100L;
        // recipeId, title, description, category, difficulty, cookingTimeMin, serving, ingredients, imageUrl, steps
        RecipeUpsertRequest req = new RecipeUpsertRequest(null, "New", "Desc", RecipeCategoryEnum.SOUP, RecipeDifficultyEnum.LOW, 30, 2.0, List.of(), "url", List.of());
        
        Recipe savedRecipe = mock(Recipe.class);
        given(savedRecipe.getId()).willReturn(1L);
        given(recipeAdapter.save(any(Recipe.class))).willReturn(savedRecipe);

        // when
        Recipe result = recipeService.createRecipe(userId, req);

        // then
        assertThat(result.getId()).isEqualTo(1L);
        verify(recipeAdapter).save(any(Recipe.class));
        verify(recipeAdapter).save(any(RecipeStats.class));
    }

    @Test
    @DisplayName("레시피 수정 - 작성자 불일치 시 예외 발생")
    void updateRecipe_forbidden() {
        // given
        Long userId = 100L;
        Long authorId = 999L;
        RecipeUpsertRequest req = new RecipeUpsertRequest(1L, "Update", "Desc", RecipeCategoryEnum.SOUP, RecipeDifficultyEnum.LOW, 30, 2.0, List.of(), "url", List.of());
        
        Recipe existingRecipe = mock(Recipe.class);
        given(existingRecipe.getAuthorId()).willReturn(authorId);
        given(recipeAdapter.findRecipeById(1L)).willReturn(Optional.of(existingRecipe));

        // when & then
        assertThatThrownBy(() -> recipeService.updateRecipe(userId, req))
            .isInstanceOf(SecurityException.class)
            .hasMessage("forbidden");
    }

    @Test
    @DisplayName("레시피 상세 조회 - 통계 정보가 없으면 IllegalStateException 발생")
    void getRecipeDetail_stats_not_found_fail() {
        // given
        Long recipeId = 1L;
        given(recipeCacheVersionResolver.getOrInit(recipeId)).willReturn(1);
        given(recipeCacheReader.getRecipeById(eq(recipeId), anyInt())).willReturn(mock(RecipeCacheValue.class));
        
        given(recipeAdapter.findRecipeStatsByRecipeId(recipeId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> recipeService.getRecipeDetail(recipeId))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("Recipe Stats not found");
    }

    @Test
    @DisplayName("레시피 수정 - 존재하지 않는 레시피 수정 시 IllegalStateException 발생")
    void updateRecipe_not_found_fail() {
        // given
        Long userId = 100L;
        RecipeUpsertRequest req = new RecipeUpsertRequest(999L, "Title", "Desc", RecipeCategoryEnum.SOUP, RecipeDifficultyEnum.LOW, 30, 2.0, List.of(), "url", List.of());
        
        given(recipeAdapter.findRecipeById(999L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> recipeService.updateRecipe(userId, req))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("Recipe not found");
    }

    @Test
    @DisplayName("레시피 삭제 - 존재하지 않는 레시피 삭제 시 IllegalStateException 발생")
    void deleteRecipe_not_found_fail() {
        // given
        Long userId = 100L;
        Long recipeId = 999L;
        given(recipeAdapter.findRecipeById(recipeId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> recipeService.deleteRecipe(userId, recipeId))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("Recipe not found");
    }

    @Test
    @DisplayName("댓글 수정 - 존재하지 않거나 비활성화된 댓글 수정 시 IllegalArgumentException 발생")
    void updateRecipeComment_not_found_fail() {
        // given
        long userId = 100L;
        long recipeId = 1L;
        long commentId = 999L;
        
        given(recipeAdapter.findRecipeCommentByIdAndRecipeIdAndStatus(eq(commentId), eq(recipeId), any()))
            .willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> recipeService.updateRecipeComment(userId, recipeId, commentId, "New Content"))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("comment not found");
    }

    @Test
    @DisplayName("댓글 수정 - 작성자가 아닌 사용자가 수정 시 SecurityException 발생")
    void updateRecipeComment_forbidden_fail() {
        // given
        long userId = 100L;
        long otherUserId = 200L;
        long recipeId = 1L;
        long commentId = 50L;
        
        com.ongi.recipe.domain.RecipeComment comment = mock(com.ongi.recipe.domain.RecipeComment.class);
        given(comment.getUserId()).willReturn(otherUserId);
        
        given(recipeAdapter.findRecipeCommentByIdAndRecipeIdAndStatus(eq(commentId), eq(recipeId), any()))
            .willReturn(Optional.of(comment));

        // when & then
        assertThatThrownBy(() -> recipeService.updateRecipeComment(userId, recipeId, commentId, "New Content"))
            .isInstanceOf(SecurityException.class)
            .hasMessage("forbidden");
    }
}
