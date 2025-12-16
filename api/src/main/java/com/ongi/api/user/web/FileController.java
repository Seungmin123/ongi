package com.ongi.api.user.web;

import com.ongi.api.user.application.FileService;
import com.ongi.api.user.web.dto.ConfirmRequest;
import com.ongi.api.user.web.dto.ConfirmResponse;
import com.ongi.api.user.web.dto.PresignRequest;
import com.ongi.api.user.web.dto.PresignResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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

	private final FileService fileService;

	@PostMapping("/public/presign")
	public PresignResponse presign(@RequestBody @Valid PresignRequest req) {
		return fileService.createProfileImagePresign(req.contentType(), req.contentLength(), req.fileName());
	}

	@PostMapping("/public/confirm")
	public ConfirmResponse confirm(@RequestBody @Valid ConfirmRequest req) {
		return fileService.confirmUploaded(req.uploadToken(), req.objectKey());
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

}
