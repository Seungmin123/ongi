package com.ongi.api.user.application.command;

import com.ongi.api.common.web.dto.UploadMeta;
import com.ongi.api.user.adapter.out.cache.UserRedisTemplate;
import com.ongi.api.user.adapter.out.cache.store.UploadSessionStore;
import com.ongi.api.user.application.component.FileClient;
import com.ongi.api.user.web.dto.ConfirmResponse;
import com.ongi.api.user.web.dto.PresignResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.net.URL;
import java.time.Duration;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class FileService {

	private static final Set<String> ALLOWED = Set.of("image/jpeg", "image/png", "image/webp");
	private static final Duration PRESIGN_TTL = Duration.ofMinutes(10);
	private static final Duration SESSION_TTL = Duration.ofMinutes(30);

	private final UploadSessionStore uploadSessionStore;
	private final FileClient fileClient;

	@Transactional(transactionManager = "transactionManager")
	public PresignResponse createProfileImagePresign(String contentType, long contentLength) {
		if (!ALLOWED.contains(contentType)) {
			// TODO Custom Exception
			throw new IllegalArgumentException("Unsupported contentType: " + contentType);
		}

		String ext = extensionFrom(contentType);

		UUID token = UUID.randomUUID();
		String tempKey = "profile/tmp/" + token + "/original." + ext;

		URL presignedUrl = fileClient.presignPut(tempKey, contentType, contentLength, PRESIGN_TTL);

		String tokenHash = UserRedisTemplate.sha256(token.toString());
		uploadSessionStore.savePresigned(
			tokenHash,
			tempKey,
			contentType,
			contentLength,
			SESSION_TTL.toSeconds()
		);

		return new PresignResponse(token, tempKey, presignedUrl.toString(), PRESIGN_TTL.toSeconds());
	}

	// TODO Custom Exception
	@Transactional(transactionManager = "transactionManager")
	public ConfirmResponse confirmUploaded(UUID token, String objectKey) {
		String tokenHash = UserRedisTemplate.sha256(token.toString());
		UploadMeta meta = uploadSessionStore.getMeta(tokenHash);

		if (meta == null) {
			throw new IllegalArgumentException("uploadToken not found or expired");
		}
		if (!meta.objectKey().equals(objectKey)) {
			throw new IllegalArgumentException("objectKey mismatch");
		}
		if (UploadSessionStoreImplStatus.isConsumed(meta.status())) {
			throw new IllegalArgumentException("already consumed");
		}

		// 파일 실체 확인
		if (!fileClient.head(objectKey)) {
			throw new IllegalArgumentException("head object not found");
		}

		uploadSessionStore.markUploaded(tokenHash);
		return new ConfirmResponse("UPLOADED");
	}

	// TODO Custom Exception
	@Transactional(transactionManager = "transactionManager")
	public String consumeAndPromoteProfileImage(UUID token, String objectKey, long userId) {
		String tokenHash = UserRedisTemplate.sha256(token.toString());

		boolean consumed = uploadSessionStore.consumeIfUploaded(tokenHash, objectKey);
		if (!consumed) {
			throw new IllegalArgumentException("Profile image session is not UPLOADED or already used");
		}

		if (!fileClient.head(objectKey)) {
			throw new IllegalArgumentException("head object not found");
		}

		String ext = objectKey.substring(objectKey.lastIndexOf('.') + 1);
		String finalKey = "profile/" + userId + "/original." + ext;
		fileClient.promote(objectKey, finalKey);

		return finalKey;
	}

	public ResponseEntity<Void> upload(String token, HttpServletRequest request, String contentType) throws IOException {
		var meta = fileClient.requireValidMeta(token);

		// Content-Type 검증(선택)
		if (contentType != null && !contentType.equalsIgnoreCase(meta.contentType())) {
			return ResponseEntity.status(409).build();
		}

		// Content-Length 검증(선택)
		long length = request.getContentLengthLong();
		if (length > 0 && length != meta.contentLength()) {
			return ResponseEntity.status(409).build();
		}

		fileClient.upload(meta.objectKey(), "original", request.getInputStream());
		fileClient.consume(token);

		return ResponseEntity.ok().build();
	}

	@Transactional(transactionManager = "transactionManager")
	public UploadMeta requireValidUploadMetaOrThrow(String uploadToken) {
		String tokenHash = UserRedisTemplate.sha256(uploadToken);
		UploadMeta meta = uploadSessionStore.getMeta(tokenHash);
		if (meta == null) throw new IllegalArgumentException("uploadToken not found or expired");
		return meta;
	}

	// 상태 문자열 비교 유틸 (문자열 오타 방지용)
	private static final class UploadSessionStoreImplStatus {
		private static boolean isConsumed(String status) {
			return "CONSUMED".equals(status);
		}
	}

	private static String extensionFrom(String contentType) {
		return switch (contentType) {
			case "image/jpeg" -> "jpg";
			case "image/png" -> "png";
			case "image/webp" -> "webp";
			default -> throw new IllegalArgumentException("Unsupported type");

		};
	}
}
