package com.ongi.api.community.adatper.out.persistence.repository;

import com.ongi.api.community.adatper.out.persistence.CommunityAttachmentEntity;
import com.ongi.api.community.adatper.out.persistence.enums.OwnerType;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CommunityAttachmentRepository extends JpaRepository<CommunityAttachmentEntity, Long> {

	@Query("select a from CommunityAttachmentEntity a where a.id in :ids")
	List<CommunityAttachmentEntity> findAllByIdIn(@Param("ids") Set<Long> ids);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select a from CommunityAttachmentEntity a where a.id in :ids")
	List<CommunityAttachmentEntity> findAllByIdInForUpdate(@Param("ids") Set<Long> ids);

	List<CommunityAttachmentEntity> findByOwnerTypeAndOwnerId(OwnerType ownerType, Long ownerId);

	@Query("select a from CommunityAttachmentEntity a where a.ownerType=:t and a.ownerId in :ids")
	List<CommunityAttachmentEntity> findByOwnerTypeAndOwnerIdIn(@Param("t") OwnerType t, @Param("ids") Set<Long> ids);
}
