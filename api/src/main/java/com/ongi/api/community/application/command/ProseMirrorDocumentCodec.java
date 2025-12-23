package com.ongi.api.community.application.command;

import java.util.LinkedHashSet;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@Component
@RequiredArgsConstructor
public class ProseMirrorDocumentCodec implements DocumentCodec {

	private final ObjectMapper objectMapper;

	/**
	 * TipTap/ProseMirror JSON 예시:
	 * {
	 *   "type":"doc",
	 *   "content":[
	 *     {"type":"paragraph","content":[{"type":"text","text":"hello"}]},
	 *     {"type":"image","attrs":{"attachmentId":123,"src":"..."}}
	 *   ]
	 * }
	 */
	@Override
	public Set<Long> extractAttachmentIds(String contentJson) {
		if (contentJson == null || contentJson.isBlank()) return Set.of();

		try {
			JsonNode root = objectMapper.readTree(contentJson);
			Set<Long> ids = new LinkedHashSet<>();
			walkAndCollectAttachmentIds(root, ids);
			return ids;
		} catch (Exception e) {
			// 정책: JSON 파싱 실패면 "첨부 없음"으로 처리(저장 로직에서 검증 실패로 막고 싶다면 예외 던지세요)
			return Set.of();
		}
	}

	@Override
	public String extractPlainText(String contentJson) {
		if (contentJson == null || contentJson.isBlank()) return "";

		try {
			JsonNode root = objectMapper.readTree(contentJson);
			StringBuilder sb = new StringBuilder();
			walkAndCollectText(root, sb);
			return normalizeSpaces(sb.toString());
		} catch (Exception e) {
			return "";
		}
	}

	private void walkAndCollectAttachmentIds(JsonNode node, Set<Long> out) {
		if (node == null || node.isNull()) return;

		// 1) attrs 안에서 id 후보 탐색
		if (node.isObject()) {
			JsonNode attrs = node.get("attrs");
			if (attrs != null && attrs.isObject()) {
				collectIdCandidates(attrs, out);
			}

			// 2) 노드 자체에 id가 있는 스키마도 대비 (드물지만)
			collectIdCandidates(node, out);
		}

		// 3) content 배열 재귀
		JsonNode content = node.get("content");
		if (content != null && content.isArray()) {
			for (JsonNode child : content) {
				walkAndCollectAttachmentIds(child, out);
			}
		}

		// 4) mark/marks 같은 확장 스키마 대비(필요시)
		JsonNode marks = node.get("marks");
		if (marks != null && marks.isArray()) {
			for (JsonNode m : marks) {
				JsonNode attrs = m.get("attrs");
				if (attrs != null && attrs.isObject()) {
					collectIdCandidates(attrs, out);
				}
			}
		}
	}

	private void collectIdCandidates(JsonNode obj, Set<Long> out) {
		// “스키마가 바뀌어도 살릴” 후보들
		// - attachmentId: 우리 도메인에서 추천
		// - id/fileId/mediaId: 흔한 네이밍
		// - attachments: [ {id:..}, ... ] 형태도 대비
		extractLongIfPresent(obj, "attachmentId", out);
		extractLongIfPresent(obj, "fileId", out);
		extractLongIfPresent(obj, "mediaId", out);
		extractLongIfPresent(obj, "id", out);

		JsonNode arr = obj.get("attachments");
		if (arr != null && arr.isArray()) {
			for (JsonNode a : arr) {
				if (a.isObject()) {
					extractLongIfPresent(a, "attachmentId", out);
					extractLongIfPresent(a, "id", out);
					extractLongIfPresent(a, "fileId", out);
				}
			}
		}
	}

	private void extractLongIfPresent(JsonNode obj, String field, Set<Long> out) {
		JsonNode v = obj.get(field);
		if (v == null || v.isNull()) return;

		// 숫자/문자 둘 다 지원
		if (v.isNumber()) {
			out.add(v.longValue());
			return;
		}
		if (v.isTextual()) {
			String s = v.asText().trim();
			if (s.isEmpty()) return;
			try {
				out.add(Long.parseLong(s));
			} catch (NumberFormatException ignored) {
			}
		}
	}

	private void walkAndCollectText(JsonNode node, StringBuilder out) {
		if (node == null || node.isNull()) return;

		if (node.isObject()) {
			// TipTap/ProseMirror text node: {"type":"text","text":"..."}
			JsonNode text = node.get("text");
			if (text != null && text.isTextual()) {
				out.append(text.asText());
			}

			// 문단/줄바꿈을 너무 붙이지 않도록 type 기반으로 공백/개행 삽입
			JsonNode type = node.get("type");
			if (type != null && type.isTextual()) {
				String t = type.asText();
				// 필요시 확장: heading, listItem 등
				if ("paragraph".equals(t) || "heading".equals(t) || "blockquote".equals(t)) {
					out.append('\n');
				}
			}
		}

		JsonNode content = node.get("content");
		if (content != null && content.isArray()) {
			for (JsonNode child : content) {
				walkAndCollectText(child, out);
			}
		}
	}

	private String normalizeSpaces(String s) {
		// 연속 개행/공백 정리 (검색용 plainText 용도)
		String trimmed = s.trim();
		trimmed = trimmed.replaceAll("[ \\t\\x0B\\f\\r]+", " ");
		trimmed = trimmed.replaceAll("\\n{3,}", "\n\n");
		return trimmed;
	}
}
