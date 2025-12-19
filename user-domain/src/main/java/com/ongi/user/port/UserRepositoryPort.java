package com.ongi.user.port;

import com.ongi.user.domain.User;
import com.ongi.user.domain.UserProfile;
import com.ongi.user.domain.UserStats;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface UserRepositoryPort {

	User save(User user);

	Optional<User> findUserById(Long id);

	List<User> findUsersByIdIn(Set<Long> ids);

	boolean existsUserByEmail(String email);

	Optional<User> findUserByEmail(String email);

	UserProfile save(UserProfile userProfile);

	Optional<UserProfile> findUserProfileById(Long id);

	Optional<UserProfile> findUserProfileByUserId(Long userId);

	Optional<UserProfile> findUserProfileByDisplayName(String displayName);

	List<UserProfile> findUserProfilesByIdIn(Set<Long> ids);

	void updatePasswordHash(Long id, String hash);

	UserStats save(UserStats userStats);

	Optional<UserStats> findUserStatsById(Long id);
}
