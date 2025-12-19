package com.ongi.api.recipe.adapter.out.persistence.repository;

import com.ongi.api.recipe.adapter.out.persistence.RecipeProcessedEventEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RecipeProcessedEventRepository extends JpaRepository<RecipeProcessedEventEntity, UUID> {

	@Modifying
	@Query(value = """
        insert ignore into recipe_processed_event(event_id, processed_at)
        values (:eventId, now())
        """, nativeQuery = true)
	int insertIgnore(@Param("eventId") String eventId);

	default boolean firstTime(String eventId) {
		return insertIgnore(eventId) == 1;
	}

}
