package com.ongi.api.user.adapter.out.persistence.repository;

import com.ongi.api.user.adapter.out.persistence.UserEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<UserEntity, Long> {

	boolean existsByEmail(String email);

	Optional<UserEntity> findByEmail(String email);

	@Modifying
	@Query("update UserEntity u set u.passwordHash = :hash where u.id = :id")
	int updatePasswordHash(@Param("id") Long id, @Param("hash") String hash);

	@Query("select u.email from UserEntity u where u.id = :userId")
	String findEmailById(@Param("userId") Long userId);
}
