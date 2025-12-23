package com.ongi.api.user.web;

import com.ongi.api.common.web.dto.ApiResponse;
import com.ongi.api.user.application.command.FileService;
import com.ongi.api.user.web.dto.ConfirmRequest;
import com.ongi.api.user.web.dto.ConfirmResponse;
import com.ongi.api.user.web.dto.PresignRequest;
import com.ongi.api.user.web.dto.PresignResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Base64;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping("/file")
@RestController
public class FileController {

	@Value("${spring.storage.base-dir:/Users/leeseumgin/Documents/온기/File}")
	private String baseDir;

	private final FileService fileService;

	@PostMapping("/public/presign")
	public ApiResponse<PresignResponse> presign(@RequestBody @Valid PresignRequest req) {
		return ApiResponse.ok(fileService.createProfileImagePresign(req.contentType(), req.contentLength()));
	}

	@PostMapping("/public/confirm")
	public ApiResponse<ConfirmResponse> confirm(@RequestBody @Valid ConfirmRequest req) {
		return ApiResponse.ok(fileService.confirmUploaded(req.uploadToken(), req.objectKey()));
	}

	// TODO S3 전 임시 Presigned Upload
	@PutMapping(
		value = "/public/put/{token}",
		consumes = MediaType.ALL_VALUE
	)
	public ResponseEntity<Void> put(
		@PathVariable String token,
		HttpServletRequest request,
		@RequestHeader(value = "Content-Type", required = false) String contentType
	) throws IOException {
		return fileService.upload(token, request, contentType);
	}

	@GetMapping("/public/**")
	public ResponseEntity<Resource> getFile(HttpServletRequest request) throws IOException {
		// /file/public/{storageKey}
		String path = request.getRequestURI()
			.replaceFirst("/file/public/", "");

		String safeKey = sanitizeKey(path);

		Path filePath = resolvePath(safeKey);
		if (!Files.exists(filePath) || !Files.isRegularFile(filePath)) {
			return ResponseEntity.notFound().build();
		}

		Resource resource = new FileSystemResource(filePath);

		String contentType = Files.probeContentType(filePath);
		if (contentType == null) {
			contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE;
		}

		return ResponseEntity.ok()
			.contentType(MediaType.parseMediaType(contentType))
			.header(HttpHeaders.CACHE_CONTROL, "max-age=3600") // CDN 흉내
			.body(resource);
	}

	private Path resolvePath(String safeKey) {
		Path root = Paths.get(baseDir).toAbsolutePath().normalize();
		Path resolved = root.resolve(safeKey).normalize();
		if (!resolved.startsWith(root)) {
			throw new IllegalArgumentException("Invalid path");
		}
		return resolved;
	}

	private String sanitizeKey(String key) {
		if (key.contains("..")) throw new IllegalArgumentException("Invalid key");
		return key.replace("\\", "/");
	}

}
