package com.ongi.api.user.persistence;

import com.ongi.user.domain.User;
import com.ongi.user.domain.UserProfile;

public class UserMapper {

	public static UserEntity toEntity(User user) {
		return UserEntity.builder()
			.email(user.getEmail())
			.passwordHash(user.getPasswordHash())
			.type(user.getType())
			.build();
	}

	public static User toDomain(UserEntity entity) {
		return User.create(entity.getEmail(), entity.getPasswordHash(), entity.getType());
	}

	public static UserProfileEntity toEntity(UserProfile userProfile) {
		return UserProfileEntity.builder()
			.userId(userProfile.getUserId())
			.displayName(userProfile.getDisplayName())
			.allergens(userProfile.getAllergens())
			.dietGoal(userProfile.getDietGoal())
			.dislikedIngredients(userProfile.getDislikedIngredients())
			.build();
	}

	public static UserProfile toDomain(UserProfileEntity entity) {
		return UserProfile.create(entity.getUserId(), entity.getDisplayName(), entity.getAllergens(), entity.getDietGoal(), entity.getDislikedIngredients());
	}
}
