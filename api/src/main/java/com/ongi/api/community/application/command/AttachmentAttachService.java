package com.ongi.api.community.application.command;

import com.ongi.api.community.adatper.out.persistence.CommunityAttachmentEntity;
import com.ongi.api.community.adatper.out.persistence.repository.CommunityAttachmentRepository;
import com.ongi.api.community.adatper.out.persistence.enums.AttachmentStatus;
import com.ongi.api.community.adatper.out.persistence.enums.OwnerType;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class AttachmentAttachService {

	private final CommunityAttachmentRepository attachmentRepository;

	@Transactional
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
}
