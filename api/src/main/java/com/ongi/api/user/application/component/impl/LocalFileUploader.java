package com.ongi.api.user.application.component.impl;

import com.ongi.api.user.application.component.FileUploader;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class LocalFileUploader implements FileUploader {

	private final Clock clock = Clock.systemUTC();

	@Value("${spring.storage.base-dir:/Users/leeseumgin/Documents/온기/File}")
	private String baseDir;

	@Value("${spring.storage.base-url:http://localhost:8080}")
	private String baseUrl;

	private final ConcurrentHashMap<String, PresignMeta> presignStore = new ConcurrentHashMap<>();

	@Override
	public URL presignPut(String objectKey, String contentType, long contentLength, Duration ttl) {
		// objectKey path traversal 방지(매우 중요)
		String safeKey = sanitizeKey(objectKey);

		String token = UUID.randomUUID().toString();
		Instant expiresAt = Instant.now(clock).plus(ttl);

		presignStore.put(token, new PresignMeta(safeKey, contentType, contentLength, expiresAt));

		try {
			return new URL(baseUrl + "/file/public/put/" + token);
		} catch (MalformedURLException e) {
			throw new IllegalStateException("Invalid baseUrl: " + baseUrl, e);
		}
	}

	@Override
	public boolean head(String objectKey) {
		String safeKey = sanitizeKey(objectKey);
		Path path = resolvePath(safeKey);
		return Files.exists(path) && Files.isRegularFile(path);
	}

	@Override
	public void upload(String objectKey, String fileName, InputStream inputStream) {
		String safeKey = sanitizeKey(objectKey);
		Path path = resolvePath(safeKey);

		try {
			Files.createDirectories(path.getParent());
			Files.copy(inputStream, path, StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			throw new RuntimeException("Local upload failed: " + safeKey, e);
		}
	}

	@Override
	public void promote(String fromObjectKey, String toObjectKey) {
		String fromKey = sanitizeKey(fromObjectKey);
		String toKey = sanitizeKey(toObjectKey);

		Path from = resolvePath(fromKey);
		Path to = resolvePath(toKey);

		try {
			Files.createDirectories(to.getParent());
			Files.move(from, to, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
		} catch (AtomicMoveNotSupportedException e) {
			try {
				Files.move(from, to, StandardCopyOption.REPLACE_EXISTING);
			} catch (IOException ex) {
				throw new RuntimeException("Local promote failed: " + fromKey + " -> " + toKey, ex);
			}
		} catch (IOException e) {
			throw new RuntimeException("Local promote failed: " + fromKey + " -> " + toKey, e);
		}
	}

	// ====== presign 메타 조회/검증용 메서드(Controller에서 사용) ======
	@Override
	public PresignMeta requireValidMeta(String token) {
		PresignMeta meta = presignStore.get(token);
		if (meta == null) throw new RuntimeException("Invalid upload token");

		Instant now = Instant.now(clock);
		if (meta.expiresAt().isBefore(now)) {
			presignStore.remove(token);
			throw new RuntimeException("Upload token expired");
		}
		return meta;
	}

	@Override
	public void consume(String token) {
		presignStore.remove(token);
	}

	private Path resolvePath(String safeKey) {
		Path root = Paths.get(baseDir).toAbsolutePath().normalize();
		Path resolved = root.resolve(safeKey).normalize();
		if (!resolved.startsWith(root)) {
			throw new IllegalArgumentException("Invalid objectKey");
		}
		return resolved;
	}

	private String sanitizeKey(String objectKey) {
		if (objectKey == null || objectKey.isBlank()) throw new IllegalArgumentException("objectKey required");
		String k = objectKey.replace("\\", "/");
		if (k.contains("..")) throw new IllegalArgumentException("Invalid objectKey");
		if (k.startsWith("/")) k = k.substring(1);
		return k;
	}

	public record PresignMeta(String objectKey, String contentType, long contentLength, Instant expiresAt) {}
}
