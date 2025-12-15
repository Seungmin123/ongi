package com.ongi.api.user.persistence.repository;

import com.ongi.api.user.persistence.UserProfileEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserProfileRepository extends JpaRepository<UserProfileEntity, Long> {

	Optional<UserProfileEntity> findByDisplayName(String displayName);
}
