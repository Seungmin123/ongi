package com.ongi.api.community.web;

import static org.mockito.ArgumentMatchers.any;
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
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.ongi.api.config.auth.JwtTokenProvider;
import com.ongi.api.community.application.command.AttachmentAttachService;
import com.ongi.api.community.application.facade.CommunityCommentEventFacade;
import com.ongi.api.community.application.facade.CommunityPostEventFacade;
import com.ongi.api.community.application.query.CommentQueryService;
import com.ongi.api.community.application.query.PostQueryService;
import com.ongi.api.community.web.dto.LikeResponse;
import com.ongi.api.community.web.dto.PostDetailResponse;
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
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@ActiveProfiles("local")
@SpringBootTest
class CommunityControllerTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private CommunityPostEventFacade postEventFacade;

    @MockitoBean
    private PostQueryService postQueryService;

    @MockitoBean
    private CommunityCommentEventFacade commentEventFacade;

    @MockitoBean
    private CommentQueryService commentQueryService;

    @MockitoBean
    private AttachmentAttachService attachmentAttachService;

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
    @DisplayName("게시글 등록 - 성공")
    void createPost_success() throws Exception {
        String token = createTestToken("1");
        String json = "{\"title\":\"New Post\", \"contentJson\":\"{}\"}";

        mockMvc.perform(post("/community/private/posts")
                .header(HttpHeaders.AUTHORIZATION, token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
            .andDo(print())
            .andExpect(status().isOk());
            
        verify(postEventFacade, times(1)).createPost(eq(1L), any());
    }

    @Test
    @DisplayName("게시글 수정 - 성공")
    void updatePost_success() throws Exception {
        String token = createTestToken("1");
        String json = "{\"postId\":1, \"title\":\"Updated\", \"contentJson\":\"{}\"}";

        mockMvc.perform(patch("/community/private/posts/1")
                .header(HttpHeaders.AUTHORIZATION, token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
            .andDo(print())
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("게시글 삭제 - 성공")
    void deletePost_success() throws Exception {
        String token = createTestToken("1");

        mockMvc.perform(delete("/community/private/posts/1")
                .header(HttpHeaders.AUTHORIZATION, token))
            .andDo(print())
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("게시글 목록 조회 - 성공")
    void getPosts_success() throws Exception {
        given(postQueryService.getPosts(any(), any(), any())).willReturn(new PageImpl<>(List.of()));

        mockMvc.perform(get("/community/public/posts/list")
                .param("page", "0")
                .param("size", "10"))
            .andDo(print())
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("게시글 상세 조회 - 성공 (비로그인)")
    void getPost_noToken_success() throws Exception {
        PostDetailResponse response = new PostDetailResponse(1L, "Title", 1, "{}", null, List.of(), 0, 0, 0, LocalDateTime.now(), false);
        given(postEventFacade.getPost(eq(null), eq(1L))).willReturn(response);

        mockMvc.perform(get("/community/public/posts/1")
                .accept(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @DisplayName("댓글 등록 - 성공")
    void createComment_success() throws Exception {
        String token = createTestToken("1");
        String json = "{\"contentJson\":\"{}\"}";

        mockMvc.perform(post("/community/private/posts/1/comments")
                .header(HttpHeaders.AUTHORIZATION, token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
            .andDo(print())
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("게시글 등록 - 실패 (필수 필드 'title' 누락)")
    void createPost_fail_missingTitle() throws Exception {
        String token = createTestToken("1");
        String json = "{\"contentJson\":\"{}\"}"; 

        mockMvc.perform(post("/community/private/posts")
                .header(HttpHeaders.AUTHORIZATION, token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
            .andDo(print())
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("댓글 등록 - 실패 (내용 누락)")
    void createComment_fail_emptyContent() throws Exception {
        String token = createTestToken("1");
        String json = "{\"contentJson\":\"\"}"; 

        mockMvc.perform(post("/community/private/posts/1/comments")
                .header(HttpHeaders.AUTHORIZATION, token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
            .andDo(print())
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("좋아요 - 실패 (토큰 없음)")
    void like_noToken_fail() throws Exception {
        mockMvc.perform(post("/community/private/posts/1/like"))
            .andDo(print())
            .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("게시글 삭제 - 실패 (잘못된 토큰)")
    void deletePost_invalidToken_fail() throws Exception {
        mockMvc.perform(delete("/community/private/posts/1")
                .header(HttpHeaders.AUTHORIZATION, "Bearer invalid.token"))
            .andDo(print())
            .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("게시글 좋아요 동시성 테스트")
    void likePost_concurrency_test() throws InterruptedException {
        int threadCount = 10;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        String token = createTestToken("123");

        for (int i = 0; i < threadCount; i++) {
            executorService.execute(() -> {
                try {
                    mockMvc.perform(post("/community/private/posts/1/like")
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
        verify(postEventFacade, times(threadCount)).like(eq(123L), eq(1L));
    }

    @Test
    @DisplayName("댓글 수정 - 성공")
    void updateComment_success() throws Exception {
        String token = createTestToken("1");
        String json = "{\"contentJson\":\"{updated}\"}";

        mockMvc.perform(patch("/community/private/posts/1/comments/100")
                .header(HttpHeaders.AUTHORIZATION, token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
            .andDo(print())
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("댓글 삭제 - 성공")
    void deleteComment_success() throws Exception {
        String token = createTestToken("1");

        mockMvc.perform(delete("/community/private/posts/1/comments/100")
                .header(HttpHeaders.AUTHORIZATION, token))
            .andDo(print())
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("댓글 목록 조회 - 성공")
    void getPostComments_success() throws Exception {
        given(commentQueryService.getComments(any(), anyLong(), any(), any())).willReturn(new PageImpl<>(List.of()));

        mockMvc.perform(get("/community/public/posts/1/comments/list")
                .param("page", "0")
                .param("size", "10"))
            .andDo(print())
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("댓글 좋아요 - 성공")
    void likeComment_success() throws Exception {
        String token = createTestToken("1");
        given(commentEventFacade.like(anyLong(), anyLong(), anyLong())).willReturn(new LikeResponse(true, 1L));

        mockMvc.perform(post("/community/private/posts/1/comments/100/like")
                .header(HttpHeaders.AUTHORIZATION, token))
            .andDo(print())
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("업로드 URL 생성 - 성공")
    void createUploadUrl_success() throws Exception {
        String token = createTestToken("1");
        String json = "{\"fileName\":\"test.jpg\", \"mimeType\":\"image/jpeg\", \"sizeBytes\":1024}";

        mockMvc.perform(post("/community/private/upload-url")
                .header(HttpHeaders.AUTHORIZATION, token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
            .andDo(print())
            .andExpect(status().isOk());
    }

    @Test
    @DisplayName("임시 첨부파일 생성 - 성공")
    void createTemp_success() throws Exception {
        String token = createTestToken("1");
        String json = "{\"storageKey\":\"key\", \"mimeType\":\"image/jpeg\", \"sizeBytes\":1024}";

        mockMvc.perform(post("/community/private/upload-url-temp")
                .header(HttpHeaders.AUTHORIZATION, token)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
            .andDo(print())
            .andExpect(status().isOk());
    }
}