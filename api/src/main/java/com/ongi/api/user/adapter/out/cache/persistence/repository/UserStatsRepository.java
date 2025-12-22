package com.ongi.api.user.adapter.out.cache.persistence.repository;

import com.ongi.api.user.adapter.out.cache.persistence.UserStatsEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserStatsRepository extends JpaRepository<UserStatsEntity, Long> {


	/*@Modifying
	@Query(value = """
        update user_stats
        set liked_recipe_count = liked_recipe_count + :delta
        where user_id = :userId
        """, nativeQuery = true)
	int incrementLikedCount(@Param("userId") long userId, @Param("delta") long delta);

	@Modifying
	@Query(value = """
        update user_stats
        set saved_recipe_count = saved_recipe_count + :delta
        where user_id = :userId
        """, nativeQuery = true)
	int incrementSavedCount(@Param("userId") long userId, @Param("delta") long delta);*/
}
