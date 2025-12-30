package com.ongi.api.user.adapter.out.persistence;

import com.ongi.api.user.adapter.out.persistence.repository.UserAllergenRepository;
import com.ongi.api.user.adapter.out.persistence.repository.UserDislikedIngredientRepository;
import com.ongi.api.user.adapter.out.persistence.repository.UserProfileRepository;
import com.ongi.api.user.adapter.out.persistence.repository.UserRepository;
import com.ongi.api.user.adapter.out.persistence.repository.UserStatsRepository;
import com.ongi.user.domain.User;
import com.ongi.user.domain.UserProfile;
import com.ongi.user.domain.UserStats;
import com.ongi.user.port.UserRepositoryPort;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@Repository
public class UserAdapter implements UserRepositoryPort {

	private final UserRepository userRepository;

	private final UserProfileRepository userProfileRepository;

	private final UserStatsRepository userStatsRepository;

	private final UserAllergenRepository userAllergenRepository;

	private final UserDislikedIngredientRepository userDislikedIngredientRepository;

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
	public List<User> findUsersByIdIn(Set<Long> ids) {
		return userRepository
			.findAllById(ids).stream()
			.map(UserMapper::toDomain)
			.toList();
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
	public Optional<UserProfile> findUserProfileByUserId(Long id) {
		return userProfileRepository
			.findByUserId(id)
			.map(UserMapper::toDomain);
	}

	@Override
	public Optional<UserProfile> findUserProfileByDisplayName(String displayName) {
		return userProfileRepository
			.findByDisplayName(displayName)
			.map(UserMapper::toDomain);
	}

	@Override
	public List<UserProfile> findUserProfilesByIdIn(Set<Long> ids) {
		return userProfileRepository
			.findAllById(ids).stream()
			.map(UserMapper::toDomain)
			.toList();
	}

	@Override
	public void updatePasswordHash(Long id, String hash) {
		userRepository.updatePasswordHash(id, hash);
	}

	@Override
	public UserStats save(UserStats userStats) {
		UserStatsEntity entity = UserMapper.toEntity(userStats);
		UserStatsEntity saved = userStatsRepository.save(entity);
		return UserMapper.toDomain(saved);
	}

	@Override
	public Optional<UserStats> findUserStatsById(Long id) {
		return userStatsRepository
			.findById(id)
			.map(UserMapper::toDomain);
	}

	@Override
	public void saveAllUserAllergen(Long userId, Set<Long> allergenIds) {
		List<UserAllergenEntity> rows = allergenIds.stream()
			.map(id -> UserAllergenEntity.builder()
				.userId(userId)
				.allergenGroupId(id)
				.build())
			.toList();

		userAllergenRepository.saveAll(rows);
	}

	@Override
	public void saveAllDislikedIngredients(Long userId, Set<Long> dislikedIngredientIds) {
		List<UserDislikedIngredientEntity> rows = dislikedIngredientIds.stream()
			.map(id -> UserDislikedIngredientEntity.builder()
				.userId(userId)
				.ingredientId(id)
				.build())
			.toList();

		userDislikedIngredientRepository.saveAll(rows);
	}
}
