package com.ongi.api.community.adatper.out.persistence.repository;

import com.ongi.api.community.adatper.out.persistence.CommunityProcessedEventEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CommunityProcessedEventRepository extends JpaRepository<CommunityProcessedEventEntity, UUID> {

	@Modifying
	@Query(value = """
        insert ignore into community_processed_event(event_id, processed_at)
        values (:eventId, now())
        """, nativeQuery = true)
	int insertIgnore(@Param("eventId") String eventId);

	default boolean firstTime(String eventId) {
		return insertIgnore(eventId) == 1;
	}

}
