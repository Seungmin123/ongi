package com.ongi.api.user.persistence.repository;

import com.ongi.api.user.persistence.UserProfileEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserProfileRepository extends JpaRepository<UserProfileEntity, Long> {
}
