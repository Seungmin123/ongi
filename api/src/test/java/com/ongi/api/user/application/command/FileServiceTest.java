package com.ongi.api.user.application.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.ongi.api.common.web.dto.UploadMeta;
import com.ongi.api.user.adapter.out.cache.store.UploadSessionStore;
import com.ongi.api.user.application.component.FileClient;
import com.ongi.api.user.web.dto.ConfirmResponse;
import com.ongi.api.user.web.dto.PresignResponse;
import java.net.URI;
import java.net.URL;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FileServiceTest {

    @Mock private UploadSessionStore uploadSessionStore;
    @Mock private FileClient fileClient;

    @InjectMocks
    private FileService fileService;

    @Test
    @DisplayName("프로필 이미지 업로드용 Presigned URL 생성 - 성공")
    void createProfileImagePresign_success() throws Exception {
        String contentType = "image/jpeg";
        long contentLength = 1024L;
        URL mockUrl = URI.create("http://mock-s3.url").toURL();
        given(fileClient.presignPut(anyString(), eq(contentType), eq(contentLength), any())).willReturn(mockUrl);

        PresignResponse result = fileService.createProfileImagePresign(contentType, contentLength);

        assertThat(result.presignedUrl()).isEqualTo(mockUrl.toString());
    }

    @Test
    @DisplayName("업로드 확인 - 성공")
    void confirmUploaded_success() {
        UUID token = UUID.randomUUID();
        String objectKey = "profile/tmp/test.jpg";
        UploadMeta meta = new UploadMeta(objectKey, "image/jpeg", 1024L, "PENDING");
        
        given(uploadSessionStore.getMeta(anyString())).willReturn(meta);
        given(fileClient.head(objectKey)).willReturn(true);

        ConfirmResponse result = fileService.confirmUploaded(token, objectKey);

        assertThat(result.status()).isEqualTo("UPLOADED");
    }

    @Test
    @DisplayName("임시 파일을 정식 경로로 이동 및 승격 - 성공")
    void consumeAndPromoteProfileImage_success() {
        UUID token = UUID.randomUUID();
        String objectKey = "profile/tmp/test.jpg";
        long userId = 1L;
        
        given(uploadSessionStore.consumeIfUploaded(anyString(), eq(objectKey))).willReturn(true);
        given(fileClient.head(objectKey)).willReturn(true);

        String result = fileService.consumeAndPromoteProfileImage(token, objectKey, userId);

        assertThat(result).isEqualTo("profile/1/original.jpg");
    }
}