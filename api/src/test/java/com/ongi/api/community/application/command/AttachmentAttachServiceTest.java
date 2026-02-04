package com.ongi.api.community.application.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.ongi.api.community.adatper.out.persistence.CommunityAttachmentEntity;
import com.ongi.api.community.adatper.out.persistence.enums.AttachmentStatus;
import com.ongi.api.community.adatper.out.persistence.enums.OwnerType;
import com.ongi.api.community.adatper.out.persistence.repository.CommunityAttachmentRepository;
import com.ongi.api.community.application.component.StorageKeyFactory;
import com.ongi.api.community.web.dto.CreateAttachmentUploadUrlRequest;
import com.ongi.api.community.web.dto.CreateAttachmentUploadUrlResponse;
import com.ongi.api.community.web.dto.CreateTempAttachmentRequest;
import com.ongi.api.community.web.dto.CreateTempAttachmentResponse;
import com.ongi.api.user.application.component.FileClient;
import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AttachmentAttachServiceTest {

    @Mock private CommunityAttachmentRepository attachmentRepository;
    @Mock private FileClient fileClient;
    @Mock private StorageKeyFactory keyFactory;

    @InjectMocks
    private AttachmentAttachService attachmentAttachService;

    @Test
    @DisplayName("첨부파일 연결 - 성공")
    void attachAllOrThrow_success() {
        // given
        Long userId = 1L;
        Set<Long> ids = Set.of(100L);
        CommunityAttachmentEntity entity = mock(CommunityAttachmentEntity.class);
        given(entity.getUploaderId()).willReturn(userId);
        given(entity.getStatus()).willReturn(AttachmentStatus.TEMP);
        given(attachmentRepository.findAllByIdInForUpdate(ids)).willReturn(List.of(entity));

        // when
        attachmentAttachService.attachAllOrThrow(userId, OwnerType.POST, 10L, ids);

        // then
        verify(entity).attach(OwnerType.POST, 10L);
    }

    @Test
    @DisplayName("첨부파일 연결 - 업로더가 다르면 SecurityException")
    void attachAllOrThrow_forbidden() {
        // given
        Long userId = 1L;
        Long otherId = 2L;
        Set<Long> ids = Set.of(100L);
        CommunityAttachmentEntity entity = mock(CommunityAttachmentEntity.class);
        given(entity.getUploaderId()).willReturn(otherId);
        given(attachmentRepository.findAllByIdInForUpdate(ids)).willReturn(List.of(entity));

        // when & then
        assertThatThrownBy(() -> attachmentAttachService.attachAllOrThrow(userId, OwnerType.POST, 10L, ids))
            .isInstanceOf(SecurityException.class)
            .hasMessage("attachment forbidden");
    }

    @Test
    @DisplayName("임시 첨부파일 생성 - S3에 파일이 없으면 실패")
    void createTemp_fail_not_in_s3() {
        // given
        CreateTempAttachmentRequest req = new CreateTempAttachmentRequest("key", "image/jpeg", 1024L, 100, 100);
        given(fileClient.head("key")).willReturn(false);

        // when & then
        assertThatThrownBy(() -> attachmentAttachService.createTemp(1L, req))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("file not uploaded yet");
    }

    @Test
    @DisplayName("업로드 URL 생성 - 성공")
    void createUploadUrl_success() throws Exception {
        // given
        CreateAttachmentUploadUrlRequest req = new CreateAttachmentUploadUrlRequest("test.jpg", "image/jpeg", 1024L);
        given(keyFactory.newTempKey(anyLong(), anyString())).willReturn("temp/key");
        given(fileClient.presignPut(eq("temp/key"), eq("image/jpeg"), eq(1024L), any())).willReturn(URI.create("http://presigned").toURL());

        // when
        CreateAttachmentUploadUrlResponse result = attachmentAttachService.createUploadUrl(1L, req);

        // then
        assertThat(result.uploadUrl()).isEqualTo("http://presigned");
        assertThat(result.storageKey()).isEqualTo("temp/key");
    }
}
