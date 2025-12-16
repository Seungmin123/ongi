package com.ongi.api.user.persistence.repository;

import com.ongi.api.user.persistence.UserStatsEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserStatsRepository extends JpaRepository<UserStatsEntity, Long> {


}
