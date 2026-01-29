package com.ongi.api.recipe.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ongi.api.common.web.dto.ApiResponse;
import com.ongi.api.config.auth.JwtTokenProvider;
import com.ongi.api.recipe.application.command.RecipeService;
import com.ongi.api.recipe.application.facade.RecipeEventFacade;
import com.ongi.api.recipe.application.query.RecipeQueryService;
import com.ongi.api.recipe.application.query.RecipeRelatedService;
import com.ongi.api.recipe.web.dto.CommentCreateRequest;
import com.ongi.api.recipe.web.dto.CommentCreateResponse;
import com.ongi.api.recipe.web.dto.LikeResponse;
import com.ongi.api.recipe.web.dto.RecipeCardResponse;
import com.ongi.api.recipe.web.dto.RecipeCommentItem;
import com.ongi.api.recipe.web.dto.RecipeDetailBaseResponse;
import com.ongi.api.recipe.web.dto.RecipeDetailResponse;
import com.ongi.api.recipe.web.dto.UserSummary;
import com.ongi.recipe.domain.enums.CommentSortOption;
import com.ongi.recipe.domain.enums.RecipeCategoryEnum;
import com.ongi.recipe.domain.search.RecipeSearchCondition;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@ActiveProfiles("local")
@SpringBootTest
class RecipeControllerTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private RecipeService recipeService;

    @MockitoBean
    private RecipeQueryService recipeQueryService;

    @MockitoBean
    private RecipeRelatedService recipeRelatedService;

    @MockitoBean
    private RecipeEventFacade recipeEventFacade;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
            .webAppContextSetup(context)
            .apply(springSecurity())
            .build();
    }

    private String createTestToken(String userId) {
        return "Bearer " + jwtTokenProvider.createAccessToken(userId, Map.of("roles", List.of("ROLE_USER")));
    }

    @Test
    @DisplayName("공개 레시피 목록 조회 - 성공 (토큰 불필요)")
    void getRecipes_success() throws Exception {
        // given
        RecipeCardResponse card = new RecipeCardResponse(
            1L, "맛있는 김치찌개", "image_url", 30, "30분", 2, "EASY", 
            100L, 20L, 10L, "KOREAN"
        );
        ApiResponse<List<RecipeCardResponse>> response = ApiResponse.ok(List.of(card));

        given(recipeService.search(any(), any()))
            .willReturn(response);

        // when & then
        mockMvc.perform(get("/recipe/public/recipe/list")
                .param("page", "0")
                .param("size", "10")
                .param("keyword", "김치")
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("레시피 등록 - 성공 (유효한 JWT)")
    void createRecipe_success() throws Exception {
        // given
        String token = createTestToken("123");
        String json = "{" +
            "\"title\":\"New Recipe\"," +
            "\"category\":\"SOUP\"," +
            "\"ingredients\":[{\"name\":\"Salt\",\"quantity\":1.0}]," +
            "\"steps\":[{\"description\":\"Step 1\"}]" +
            "}";

        // when & then
        mockMvc.perform(post("/recipe/private/recipe")
                .header(HttpHeaders.AUTHORIZATION, token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));
            
        verify(recipeEventFacade, times(1)).createRecipe(eq(123L), any());
    }

    @Test
    @DisplayName("레시피 등록 - 실패 (잘못된 JWT)")
    void createRecipe_fail_invalidToken() throws Exception {
        // given
        String invalidToken = "Bearer invalid.token.here";
        String json = "{\"title\":\"New Recipe\", \"category\":\"SOUP\"}";

        // when & then
        mockMvc.perform(post("/recipe/private/recipe")
                .header(HttpHeaders.AUTHORIZATION, invalidToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
            .andDo(print())
            .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("좋아요 - 성공 (유효한 JWT)")
    void like_success() throws Exception {
        // given
        String token = createTestToken("456");
        LikeResponse response = new LikeResponse(true, 10L);
        given(recipeEventFacade.like(eq(456L), anyLong()))
            .willReturn(response);

        // when & then
        mockMvc.perform(put("/recipe/private/{recipeId}/like", 1L)
                .header(HttpHeaders.AUTHORIZATION, token))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.likedByMe").value(true));
    }

    @Test
    @DisplayName("레시피 상세 조회 - 성공 (토큰 포함)")
    void getRecipeDetail_withToken_success() throws Exception {
        // given
        String token = createTestToken("123");
        RecipeDetailBaseResponse base = new RecipeDetailBaseResponse(
            "img", "Title", 30, "30m", 2, "EASY", RecipeCategoryEnum.SOUP, 
            10L, 5L, 2L, null, null
        );
        RecipeDetailResponse detail = new RecipeDetailResponse(base, false, false);
        
        given(recipeEventFacade.view(eq(123L), anyLong()))
            .willReturn(detail);

        // when & then
        mockMvc.perform(get("/recipe/public/recipe/{recipeId}", 1L)
                .header(HttpHeaders.AUTHORIZATION, token)
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("레시피 등록 - 실패 (토큰 없음)")
    void createRecipe_noToken_fail() throws Exception {
        String json = "{\"title\":\"No Token Recipe\", \"category\":\"SOUP\"}";

        mockMvc.perform(post("/recipe/private/recipe")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
            .andDo(print())
            .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("좋아요 - 실패 (토큰 없음)")
    void like_noToken_fail() throws Exception {
        mockMvc.perform(put("/recipe/private/{recipeId}/like", 1L))
            .andDo(print())
            .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("레시피 삭제 - 실패 (토큰 없음)")
    void deleteRecipe_noToken_fail() throws Exception {
        mockMvc.perform(delete("/recipe/private/recipe/{recipeId}", 1L))
            .andDo(print())
            .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("공개 레시피 상세 조회 - 성공 (토큰 없이도 가능)")
    void getRecipeDetail_noToken_success() throws Exception {
        // given
        RecipeDetailBaseResponse base = new RecipeDetailBaseResponse(
            "img", "Title", 30, "30m", 2, "EASY", RecipeCategoryEnum.SOUP, 
            10L, 5L, 2L, null, null
        );
        RecipeDetailResponse detail = new RecipeDetailResponse(base, false, false);
        
        given(recipeEventFacade.view(eq(null), anyLong()))
            .willReturn(detail);

        // when & then
        mockMvc.perform(get("/recipe/public/recipe/{recipeId}", 1L)
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("레시피 등록 - 실패 (필수 필드 'title' 누락)")
    void createRecipe_validation_fail_missing_title() throws Exception {
        // given
        String token = createTestToken("123");
        // title이 누락된 JSON
        String json = "{\"category\":\"SOUP\", \"ingredients\":[{\"name\":\"Salt\",\"quantity\":1.0}], \"steps\":[{\"description\":\"Step 1\"}]}";

        // when & then
        mockMvc.perform(post("/recipe/private/recipe")
                .header(HttpHeaders.AUTHORIZATION, token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
            .andDo(print())
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("레시피 등록 - 실패 (ingredients 리스트 비어있음)")
    void createRecipe_validation_fail_empty_ingredients() throws Exception {
        // given
        String token = createTestToken("123");
        String json = "{\"title\":\"Title\", \"category\":\"SOUP\", \"ingredients\":[], \"steps\":[{\"description\":\"Step 1\"}]}";

        // when & then
        mockMvc.perform(post("/recipe/private/recipe")
                .header(HttpHeaders.AUTHORIZATION, token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
            .andDo(print())
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("레시피 등록 - 실패 (재료 수량이 음수)")
    void createRecipe_validation_fail_negative_quantity() throws Exception {
        // given
        String token = createTestToken("123");
        String json = "{\"title\":\"Title\", \"category\":\"SOUP\", \"ingredients\":[{\"name\":\"Salt\",\"quantity\":-1.0}], \"steps\":[{\"description\":\"Step 1\"}]}";

        // when & then
        mockMvc.perform(post("/recipe/private/recipe")
                .header(HttpHeaders.AUTHORIZATION, token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
            .andDo(print())
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("레시피 수정 - 실패 (잘못된 Enum 타입 값)")
    void updateRecipe_validation_fail_invalid_enum() throws Exception {
        // given
        String token = createTestToken("123");
        // category에 존재하지 않는 값 주입
        String json = "{\"recipeId\":1, \"title\":\"Title\", \"category\":\"INVALID_CATEGORY\"}";

        // when & then
        mockMvc.perform(patch("/recipe/private/recipe")
                .header(HttpHeaders.AUTHORIZATION, token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
            .andDo(print())
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("댓글 등록 - 실패 (내용이 너무 김 - 1000자 초과)")
    void createComment_validation_fail_too_long() throws Exception {
        // given
        String token = createTestToken("123");
        String longContent = "a".repeat(1001);
        String json = "{\"content\":\"" + longContent + "\"}";

        // when & then
        mockMvc.perform(post("/recipe/private/{recipeId}/comment", 1L)
                .header(HttpHeaders.AUTHORIZATION, token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
            .andDo(print())
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("공개 레시피 목록 조회 - 실패 (ID 타입 불일치)")
    void getRecipes_typeMismatch_fail() throws Exception {
        mockMvc.perform(get("/recipe/public/recipe/list")
                .param("ingredientId", "not-a-long")
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("댓글 조회 - 실패 (페이지 번호 음수)")
    void getComments_validation_fail_negative_page() throws Exception {
        // given
        String token = createTestToken("123");

        // when & then
        mockMvc.perform(get("/recipe/private/{recipeId}/comment", 1L)
                .header(HttpHeaders.AUTHORIZATION, token)
                .param("page", "-5")
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isBadRequest());
    }
}