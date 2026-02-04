package com.ongi.api.recipe.application.query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import com.ongi.api.ingredients.adapter.out.persistence.RecipeRelatedConfigEntity;
import com.ongi.api.ingredients.adapter.out.persistence.projection.RelatedRecipeRow;
import com.ongi.api.ingredients.adapter.out.persistence.repository.RecipeRelatedConfigRepository;
import com.ongi.api.ingredients.adapter.out.persistence.repository.RecipeRelatedNativeRepository;
import com.ongi.api.recipe.adapter.out.persistence.metrics.repository.RecipeDailyMetricsRepository;
import com.ongi.api.recipe.adapter.out.persistence.projection.RecipeCardRow;
import com.ongi.api.recipe.adapter.out.persistence.repository.RecipeCardQueryRepository;
import com.ongi.api.recipe.web.dto.RelatedRecipeItem;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RecipeRelatedServiceTest {

    @Mock
    private RecipeRelatedNativeRepository relatedRepository;
    @Mock
    private RecipeCardQueryRepository cardRepository;
    @Mock
    private RecipeRelatedConfigRepository configRepository;
    @Mock
    private RecipeDailyMetricsRepository dailyMetricsRepository;

    @InjectMocks
    private RecipeRelatedService recipeRelatedService;

    @Test
    @DisplayName("연관 레시피 조회 - 성공 및 데이터 매칭 확인")
    void findRelatedWithPopularityBoost_success() {
        // given
        Long recipeId = 1L;
        int limit = 5;

        // 1. 설정값 Mocking
        RecipeRelatedConfigEntity cfg = mock(RecipeRelatedConfigEntity.class);
        given(cfg.getCategoryAlpha()).willReturn(1.0);
        given(cfg.getIdfBase()).willReturn(1.0);
        given(configRepository.findSingleton()).willReturn(Optional.of(cfg));

        // 2. 연관 레시피 로우 Mocking
        RelatedRecipeRow row1 = mock(RelatedRecipeRow.class);
        given(row1.recipeId()).willReturn(10L);
        given(row1.score()).willReturn(0.95);

        given(relatedRepository.findRelatedWithPopularityBoost(
            eq(recipeId), eq(limit), 
            anyDouble(), anyDouble(), anyDouble(), anyInt(), anyDouble(),
            anyInt(), anyDouble(), anyInt(), anyDouble(), anyDouble()
        )).willReturn(List.of(row1));

        // 3. 카드 정보 Mocking
        RecipeCardRow card1 = mock(RecipeCardRow.class);
        given(card1.recipeId()).willReturn(10L);
        given(card1.title()).willReturn("연관 레시피 1");

        given(cardRepository.findCardsByIds(any())).willReturn(Map.of(10L, card1));

        // when
        List<RelatedRecipeItem> result = recipeRelatedService.findRelatedWithPopularityBoost(recipeId, limit);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).recipeId()).isEqualTo(10L);
        assertThat(result.get(0).title()).isEqualTo("연관 레시피 1");
    }

    @Test
    @DisplayName("연관 레시피 조회 - 결과가 없는 경우 빈 리스트 반환")
    void findRelatedWithPopularityBoost_empty() {
        // given
        RecipeRelatedConfigEntity cfg = mock(RecipeRelatedConfigEntity.class);
        given(configRepository.findSingleton()).willReturn(Optional.of(cfg));
        
        given(relatedRepository.findRelatedWithPopularityBoost(
            anyLong(), anyInt(), 
            anyDouble(), anyDouble(), anyDouble(), anyInt(), anyDouble(),
            anyInt(), anyDouble(), anyInt(), anyDouble(), anyDouble()
        )).willReturn(List.of());

        // when
        List<RelatedRecipeItem> result = recipeRelatedService.findRelatedWithPopularityBoost(1L, 5);

        // then
        assertThat(result).isEmpty();
    }
}