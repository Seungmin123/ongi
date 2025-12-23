package com.ongi.api.community.application.command;

import com.ongi.api.community.adatper.out.persistence.CommunityAttachmentEntity;
import com.ongi.api.community.adatper.out.persistence.repository.CommunityAttachmentRepository;
import com.ongi.api.community.adatper.out.persistence.enums.AttachmentStatus;
import com.ongi.api.community.adatper.out.persistence.enums.OwnerType;
import com.ongi.api.community.application.component.StorageKeyFactory;
import com.ongi.api.community.web.dto.CreateAttachmentUploadUrlRequest;
import com.ongi.api.community.web.dto.CreateAttachmentUploadUrlResponse;
import com.ongi.api.community.web.dto.CreateTempAttachmentRequest;
import com.ongi.api.community.web.dto.CreateTempAttachmentResponse;
import com.ongi.api.user.application.component.FileClient;
import java.net.URL;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class AttachmentAttachService {

	private final CommunityAttachmentRepository attachmentRepository;

	private final FileClient fileClient;

	private final StorageKeyFactory keyFactory;

	@Transactional(transactionManager = "transactionManager")
	public void attachAllOrThrow(Long uploaderId, OwnerType ownerType, Long ownerId, Set<Long> ids) {
		if (ids == null || ids.isEmpty()) return;

		List<CommunityAttachmentEntity> list = attachmentRepository.findAllByIdInForUpdate(ids);

		if (list.size() != ids.size()) {
			throw new IllegalArgumentException("attachment not found");
		}

		for (var a : list) {
			if (!a.getUploaderId().equals(uploaderId)) throw new SecurityException("attachment forbidden");
			if (a.getStatus() != AttachmentStatus.TEMP) throw new IllegalStateException("attachment not TEMP");
			a.attach(ownerType, ownerId);
		}
	}

	@Transactional(transactionManager = "transactionManager")
	public CreateTempAttachmentResponse createTemp(Long uploaderId, CreateTempAttachmentRequest req) {
		// 최소 검증
		if (req.storageKey() == null || req.storageKey().isBlank()) throw new IllegalArgumentException("storageKey required");
		if (req.mimeType() == null || req.mimeType().isBlank()) throw new IllegalArgumentException("mimeType required");
		if (req.sizeBytes() <= 0) throw new IllegalArgumentException("sizeBytes invalid");

		if (!fileClient.head(req.storageKey())) {
			throw new IllegalStateException("file not uploaded yet");
		}

		var e = CommunityAttachmentEntity.createTemp(
			uploaderId,
			req.storageKey(),
			req.mimeType(),
			req.sizeBytes(),
			req.width(),
			req.height()
		);

		e = attachmentRepository.save(e);
		return new CreateTempAttachmentResponse(e.getId());
	}

	public CreateAttachmentUploadUrlResponse createUploadUrl(Long uploaderId, CreateAttachmentUploadUrlRequest req) {
		if (req.mimeType() == null || req.mimeType().isBlank()) throw new IllegalArgumentException("mimeType required");
		if (req.sizeBytes() <= 0) throw new IllegalArgumentException("sizeBytes invalid");

		String storageKey = keyFactory.newTempKey(uploaderId, req.fileName());
		var ttl = java.time.Duration.ofMinutes(10);

		URL uploadUrl = fileClient.presignPut(storageKey, req.mimeType(), req.sizeBytes(), ttl);

		return new CreateAttachmentUploadUrlResponse(
			storageKey,
			uploadUrl.toString(),
			req.sizeBytes(),
			ttl.toSeconds()
		);
	}

}
