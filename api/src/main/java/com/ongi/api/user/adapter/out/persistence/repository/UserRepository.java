package com.ongi.api.user.adapter.out.persistence.repository;

import com.ongi.api.user.adapter.out.persistence.UserEntity;
import jakarta.persistence.QueryHint;
import java.util.Optional;
import java.util.stream.Stream;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;

import static org.hibernate.jpa.QueryHints.HINT_FETCH_SIZE;

public interface UserRepository extends JpaRepository<UserEntity, Long> {

	boolean existsByEmail(String email);

	Optional<UserEntity> findByEmail(String email);

	@Modifying
	@Query("update UserEntity u set u.passwordHash = :hash where u.id = :id")
	int updatePasswordHash(@Param("id") Long id, @Param("hash") String hash);

	@Query("select u.email from UserEntity u where u.id = :userId")
	String findEmailById(@Param("userId") Long userId);

	@QueryHints(value = @QueryHint(name = HINT_FETCH_SIZE, value = "" + Integer.MIN_VALUE))
	@Query("SELECT u FROM UserEntity u")
	Stream<UserEntity> streamAll();

	@QueryHints(value = @QueryHint(name = HINT_FETCH_SIZE, value = "" + Integer.MIN_VALUE))
	@Query("SELECT u FROM UserEntity u WHERE u.marketingAgreed = true")
	Stream<UserEntity> streamAllByMarketingAgreedTrue();
}
