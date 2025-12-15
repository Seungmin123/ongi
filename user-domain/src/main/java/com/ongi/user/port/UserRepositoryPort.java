package com.ongi.user.port;

import com.ongi.user.domain.User;
import com.ongi.user.domain.UserProfile;
import java.util.Optional;

public interface UserRepositoryPort {

	User save(User user);

	Optional<User> findUserById(Long id);

	boolean existsUserByEmail(String email);

	Optional<User> findUserByEmail(String email);

	UserProfile save(UserProfile userProfile);

	Optional<UserProfile> findUserProfileById(Long id);

	Optional<UserProfile> findUserProfileByDisplayName(String displayName);
}
