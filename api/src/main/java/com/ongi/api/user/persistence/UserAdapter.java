package com.ongi.api.user.persistence;

import com.ongi.api.user.persistence.repository.UserProfileRepository;
import com.ongi.api.user.persistence.repository.UserRepository;
import com.ongi.user.domain.User;
import com.ongi.user.domain.UserProfile;
import com.ongi.user.port.UserRepositoryPort;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@Repository
public class UserAdapter implements UserRepositoryPort {

	private final UserRepository userRepository;

	private final UserProfileRepository userProfileRepository;

	@Override
	public User save(User user) {
		UserEntity entity = UserMapper.toEntity(user);
		UserEntity saved = userRepository.save(entity);
		return UserMapper.toDomain(saved);
	}

	@Override
	public Optional<User> findUserById(Long id) {
		return userRepository
			.findById(id)
			.map(UserMapper::toDomain);
	}

	@Override
	public boolean existsUserByEmail(String email) {
		return userRepository.existsByEmail(email);
	}

	@Override
	public Optional<User> findUserByEmail(String email) {
		return userRepository
			.findByEmail(email)
			.map(UserMapper::toDomain);
	}

	@Override
	public UserProfile save(UserProfile userProfile) {
		UserProfileEntity entity = UserMapper.toEntity(userProfile);
		UserProfileEntity saved = userProfileRepository.save(entity);
		return UserMapper.toDomain(saved);
	}

	@Override
	public Optional<UserProfile> findUserProfileById(Long id) {
		return userProfileRepository
			.findById(id)
			.map(UserMapper::toDomain);
	}

	@Override
	public Optional<UserProfile> findUserProfileByDisplayName(String displayName) {
		return userProfileRepository
			.findByDisplayName(displayName)
			.map(UserMapper::toDomain);
	}

	@Override
	public void updatePasswordHash(Long id, String hash) {
		userRepository.updatePasswordHash(id, hash);
	}
}
