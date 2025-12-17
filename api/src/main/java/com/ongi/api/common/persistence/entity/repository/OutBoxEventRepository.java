package com.ongi.api.common.persistence.entity.repository;

import com.ongi.api.common.persistence.entity.OutBoxEventEntity;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OutBoxEventRepository extends JpaRepository<OutBoxEventEntity, Long> {

	@Query(value = """
        select outbox_event_id
        from outbox_event
        where status = 'PENDING'
          and next_attempt_at <= now()
        order by outbox_event_id
        limit :limit
        for update skip locked
        """, nativeQuery = true)
	List<Long> findPendingIdsForUpdateSkipLocked(@Param("limit") int limit);

	@Modifying
	@Query(value = """
        update outbox_event
        set status = 'PROCESSING',
            locked_by = :lockedBy,
            locked_at = now()
        where outbox_event_id in (:ids)
          and status = 'PENDING'
        """, nativeQuery = true)
	int markProcessing(@Param("ids") List<Long> ids, @Param("lockedBy") String lockedBy);

	List<OutBoxEventEntity> findByIdIn(List<Long> ids);

	@Modifying
	@Query(value = """
        update outbox_event
        set status = 'SUCCESS',
            sent_at = now(),
            locked_by = null,
            locked_at = null
        where outbox_event_id = :id
          and status = 'PROCESSING'
        """, nativeQuery = true)
	int markSuccess(@Param("id") long id);

	@Modifying
	@Query(value = """
        update outbox_event
        set status = 'PENDING',
            retry_count = retry_count + 1,
            last_error = :err,
            next_attempt_at = :nextAttemptAt,
            locked_by = null,
            locked_at = null
        where outbox_event_id = :id
          and status = 'PROCESSING'
        """, nativeQuery = true)
	int markRetry(@Param("id") long id, @Param("err") String err,
		@Param("nextAttemptAt") LocalDateTime nextAttemptAt);

	@Modifying
	@Query(value = """
        update outbox_event
        set status = 'FAILED',
            retry_count = retry_count + 1,
            last_error = :err,
            locked_by = null,
            locked_at = null
        where outbox_event_id = :id
          and status = 'PROCESSING'
        """, nativeQuery = true)
	int markFailed(@Param("id") long id, @Param("err") String err);

	@Modifying
	@Query(value = """
        update outbox_event
        set status = 'PENDING',
            locked_by = null,
            locked_at = null,
            next_attempt_at = now()
        where status = 'PROCESSING'
          and locked_at < date_sub(now(), interval :staleSeconds second)
        """, nativeQuery = true)
	int reclaimStale(@Param("staleSeconds") int staleSeconds);

}
