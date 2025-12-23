package com.ongi.api.community.adatper.out.persistence;

import com.ongi.api.common.persistence.entity.BaseTimeEntity;
import com.ongi.api.community.adatper.out.persistence.enums.AttachmentStatus;
import com.ongi.api.community.adatper.out.persistence.enums.OwnerType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name="community_attachment",
	indexes = {
		@Index(name="idx_att_uploader_status_id", columnList="uploader_id, status, attachment_id"),
		@Index(name="idx_att_owner", columnList="owner_type, owner_id, attachment_id"),
		@Index(name="idx_att_status_id", columnList="status, attachment_id")
	}
)
public class CommunityAttachmentEntity extends BaseTimeEntity {

	@Id @GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="attachment_id")
	private Long id;

	@Column(name="uploader_id", nullable=false)
	private Long uploaderId; // 파일 업로드 한 유저 Id

	@Enumerated(EnumType.STRING)
	@Column(name="owner_type", length=20)
	private OwnerType ownerType; // POST, COMMENT (TEMP면 null)

	@Column(name="owner_id")
	private Long ownerId; // TEMP면 null / POST, COMMENT 등의 Id

	@Enumerated(EnumType.STRING)
	@Column(name="status", nullable=false, length=20)
	private AttachmentStatus status; // TEMP, ATTACHED, DELETED

	@Column(name="storage_key", nullable=false, length=500)
	private String storageKey; // 실제 저장소 내 위치

	@Column(name="mime_type", nullable=false, length=100)
	private String mimeType;

	@Column(name="size_bytes", nullable=false)
	private long sizeBytes;

	@Column(name="width")
	private Integer width;

	@Column(name="height")
	private Integer height;

	public static CommunityAttachmentEntity createTemp(Long uploaderId, String storageKey,
		String mimeType, long sizeBytes, Integer w, Integer h) {
		CommunityAttachmentEntity e = new CommunityAttachmentEntity();
		e.uploaderId = uploaderId;
		e.storageKey = storageKey;
		e.mimeType = mimeType;
		e.sizeBytes = sizeBytes;
		e.width = w;
		e.height = h;
		e.status = AttachmentStatus.TEMP;
		return e;
	}

	public void attach(OwnerType ownerType, Long ownerId) {
		if (status != AttachmentStatus.TEMP) throw new IllegalStateException("not TEMP");
		this.ownerType = ownerType;
		this.ownerId = ownerId;
		this.status = AttachmentStatus.ATTACHED;
	}

	public boolean softDelete() {
		if (status == AttachmentStatus.DELETED) return false;
		status = AttachmentStatus.DELETED;
		return true;
	}

}
