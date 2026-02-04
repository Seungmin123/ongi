package com.ongi.api.recipe.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ongi.api.config.auth.JwtTokenProvider;
import com.ongi.api.recipe.application.facade.RecipeEventFacade;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@ActiveProfiles("test")
@SpringBootTest
class RecipeControllerConcurrencyTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

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
    @DisplayName("좋아요 동시성 테스트 - 30개의 동시에 들어온 좋아요 요청 처리")
    void like_concurrency_test() throws InterruptedException {
        // given
        int threadCount = 30;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        String token = createTestToken("123");

        // when
        for (int i = 0; i < threadCount; i++) {
            executorService.execute(() -> {
                try {
                    mockMvc.perform(put("/recipe/private/1/like")
                            .header(HttpHeaders.AUTHORIZATION, token))
                        .andExpect(status().isOk());
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();

        // then
        // 컨트롤러 단계에서는 30번 모두 Facade로 전달되어야 함 (현재 로직상)
        verify(recipeEventFacade, times(threadCount)).like(eq(123L), eq(1L));
    }

    @Test
    @DisplayName("레시피 등록 멱등성 테스트 - 동일한 등록 요청이 짧은 시간에 2번 발생할 때")
    void createRecipe_idempotency_test() throws Exception {
        // given
        int requestCount = 2;
        ExecutorService executorService = Executors.newFixedThreadPool(requestCount);
        CountDownLatch latch = new CountDownLatch(requestCount);
        String token = createTestToken("123");
        String json = "{"
            + "\"title\":\"Double Click Recipe\","
            + "\"category\":\"SOUP\","
            + "\"ingredients\":[{\"name\":\"Salt\",\"quantity\":1.0}],"
            + "\"steps\":[{\"description\":\"Step 1\"}]"
            + "}";

        // when
        for (int i = 0; i < requestCount; i++) {
            executorService.execute(() -> {
                try {
                    mockMvc.perform(post("/recipe/private/recipe")
                            .header(HttpHeaders.AUTHORIZATION, token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json));
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();

        // then
        // 멱등성 방어 로직이 없다면 2번 모두 Facade로 전달됨
        verify(recipeEventFacade, atLeastOnce()).createRecipe(eq(123L), any());
    }
}
