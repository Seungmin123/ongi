package com.ongi.api.community.application.command;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

class ProseMirrorDocumentCodecTest {

    private final ProseMirrorDocumentCodec codec = new ProseMirrorDocumentCodec(new ObjectMapper());

    @Test
    @DisplayName("첨부파일 ID 추출 - 다양한 스키마 대응 확인")
    void extractAttachmentIds_success() {
        // given: TipTap 스타일의 복잡한 JSON
        String json = """
            {
              "type": "doc",
              "content": [
                {
                  "type": "paragraph",
                  "content": [{"type": "text", "text": "hello"}]
                },
                {
                  "type": "image",
                  "attrs": { "attachmentId": 100, "src": "..." }
                },
                {
                  "type": "media",
                  "attrs": { "id": "200" }
                }
              ]
            }
            """;

        // when
        Set<Long> ids = codec.extractAttachmentIds(json);

        // then
        assertThat(ids).containsExactlyInAnyOrder(100L, 200L);
    }

    @Test
    @DisplayName("평문 텍스트 추출 - 개행 및 공백 정규화 확인")
    void extractPlainText_success() {
        // given
        String json = """
            {
              "type": "doc",
              "content": [
                { "type": "heading", "content": [{"type": "text", "text": "Title"}] },
                { "type": "paragraph", "content": [{"type": "text", "text": "Line 1"}] },
                { "type": "paragraph", "content": [{"type": "text", "text": "Line 2"}] }
              ]
            }
            """;

        // when
        String text = codec.extractPlainText(json);

        // then
        assertThat(text).isEqualTo("Title\nLine 1\nLine 2");
    }
}