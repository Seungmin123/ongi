package com.ongi.api.user.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ongi.api.common.web.dto.JwtTokens;
import com.ongi.api.config.auth.JwtTokenProvider;
import com.ongi.api.user.application.command.AuthService;
import com.ongi.api.user.application.command.UserService;
import com.ongi.api.user.web.dto.MyPageResponse;
import java.time.LocalDateTime;
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
class UserControllerTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private AuthService authService;

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
    @DisplayName("이메일 인증 요청 - 성공")
    void signUpEmailRequest_success() throws Exception {
        String json = "{\"email\":\"test@example.com\"}";

        mockMvc.perform(post("/user/public/signup/email-request")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("로그인 - 성공")
    void login_success() throws Exception {
        String json = "{\"email\":\"test@example.com\", \"password\":\"password123\"}";
        JwtTokens tokens = new JwtTokens("access", "refresh");
        given(authService.login(any())).willReturn(tokens);

        mockMvc.perform(post("/user/public/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.accessToken").value("access"));
    }

    @Test
    @DisplayName("내 정보 조회 - 성공 (JWT 인증)")
    void getMe_success() throws Exception {
        String token = createTestToken("1");
        given(userService.getMe(eq(1L), any())).willReturn(new MyPageResponse(null, null, null));

        mockMvc.perform(get("/user/private/me")
                .header(HttpHeaders.AUTHORIZATION, token)
                .param("include", "BASIC"))
            .andDo(print())
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("이메일 인증 요청 - 실패 (이메일 형식 오류)")
    void signUpEmailRequest_fail_invalidEmail() throws Exception {
        String json = "{\"email\":\"invalid-email-format\"}";

        mockMvc.perform(post("/user/public/signup/email-request")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
            .andDo(print())
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("회원가입 - 실패 (비밀번호 길이 짧음)")
    void signUp_fail_shortPassword() throws Exception {
        String json = "{"
            + "\"email\":\"test@example.com\",\n" +
            "\"password\":\"short\",\n" +
            "\"displayName\":\"nick\",\n" +
            "\"signUpToken\":\"token\"\n" +
            "}";

        mockMvc.perform(post("/user/public/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
            .andDo(print())
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("기본 정보 수정 - 실패 (필드 제약 조건 위반)")
    void updateBasic_fail_validation() throws Exception {
        String token = createTestToken("1");
        String json = "{\"birth\":\"99-99-99\"}";

        mockMvc.perform(patch("/user/private/me/basic")
                .header(HttpHeaders.AUTHORIZATION, token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
            .andDo(print())
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("로그인 동시성 테스트 - 10개 동시 요청")
    void login_concurrency_test() throws InterruptedException {
        int threadCount = 10;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        String json = "{\"email\":\"test@example.com\", \"password\":\"password123\"}";
        
        JwtTokens tokens = new JwtTokens("access", "refresh");
        given(authService.login(any())).willReturn(tokens);

        for (int i = 0; i < threadCount; i++) {
            executorService.execute(() -> {
                try {
                    mockMvc.perform(post("/user/public/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(json))
                        .andExpect(status().isOk());
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        verify(authService, times(threadCount)).login(any());
    }

    @Test
    @DisplayName("토큰 갱신 - 성공")
    void refreshToken_success() throws Exception {
        String token = createTestToken("1");
        String json = "{\"refreshToken\":\"refresh-token\"}";
        given(authService.refresh(any())).willReturn(new JwtTokens("new-access", "new-refresh"));

        mockMvc.perform(post("/user/private/refresh")
                .header(HttpHeaders.AUTHORIZATION, token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.accessToken").value("new-access"));
    }

    @Test
    @DisplayName("이메일 인증 확인 - 성공")
    void signUpEmailConfirm_success() throws Exception {
        String json = "{\"email\":\"test@example.com\", \"code\":\"123456\"}";

        mockMvc.perform(post("/user/public/signup/email-confirm")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
            .andDo(print())
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("이메일 찾기 - 성공")
    void findById_success() throws Exception {
        mockMvc.perform(get("/user/public/id/find")
                .param("email", "test@example.com")
                .param("displayName", "nick"))
            .andDo(print())
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("비밀번호 재설정 요청 - 성공")
    void resetPassword_success() throws Exception {
        String json = "{\"email\":\"test@example.com\"}";

        mockMvc.perform(post("/user/public/password/reset-request")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
            .andDo(print())
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("비밀번호 재설정 확인 - 성공")
    void resetPasswordConfirm_success() throws Exception {
        String json = "{\"token\":\"reset-token\", \"newPassword\":\"newPassword123\"}";

        mockMvc.perform(post("/user/public/password/reset-confirm")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
            .andDo(print())
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("요약 정보 수정 - 성공")
    void updateSummary_success() throws Exception {
        String token = createTestToken("1");
        String json = "{\"displayName\":\"updatedNick\"}";

        mockMvc.perform(patch("/user/private/me/summary")
                .header(HttpHeaders.AUTHORIZATION, token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
            .andDo(print())
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("개인화 정보 수정 - 성공")
    void updatePersonalization_success() throws Exception {
        String token = createTestToken("1");
        String json = "{\"allergenGroupIds\":[1,2], \"dislikedIngredientIds\":[3,4], \"dietGoal\":2000.0}";

        mockMvc.perform(patch("/user/private/me/personalization")
                .header(HttpHeaders.AUTHORIZATION, token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
            .andDo(print())
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("내 통계 조회 - 성공")
    void meStats_success() throws Exception {
        String token = createTestToken("1");
        given(userService.getMyPageStats(anyLong())).willReturn(new com.ongi.api.user.web.dto.MyPageStatsResponse(0L, 0L, 0L, 0L, 0L));

        mockMvc.perform(get("/user/private/me/stats")
                .header(HttpHeaders.AUTHORIZATION, token))
            .andDo(print())
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("비밀번호 재설정 확인 - 실패 (비밀번호 규칙 위반)")
    void resetPasswordConfirm_fail_validation() throws Exception {
        String json = "{\"token\":\"reset-token\", \"newPassword\":\"short\"}";

        mockMvc.perform(post("/user/public/password/reset-confirm")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
            .andDo(print())
            .andExpect(status().isBadRequest());
    }
}