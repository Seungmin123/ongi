package com.ongi.api.user.persistence.repository;

import com.ongi.api.user.persistence.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<UserEntity, Long> {
}
