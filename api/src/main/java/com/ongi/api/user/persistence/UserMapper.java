package com.ongi.api.user.persistence;

import com.ongi.user.domain.User;
import com.ongi.user.domain.UserProfile;

public class UserMapper {

	public static UserEntity toEntity(User user) {
		if(user.getId() == null) {
			return UserEntity.builder()
				.email(user.getEmail())
				.passwordHash(user.getPasswordHash())
				.type(user.getType())
				.tier(user.getTier())
				.build();
		} else {
			return UserEntity.builder()
				.id(user.getId())
				.email(user.getEmail())
				.passwordHash(user.getPasswordHash())
				.type(user.getType())
				.tier(user.getTier())
				.build();
		}
	}

	public static User toDomain(UserEntity entity) {
		return User.create(entity.getId(), entity.getEmail(), entity.getPasswordHash(), entity.getType(), entity.getTier());
	}

	public static UserProfileEntity toEntity(UserProfile userProfile) {
		if(userProfile.getId() == null) {
			return UserProfileEntity.builder()
				.userId(userProfile.getUserId())
				.displayName(userProfile.getDisplayName())
				.allergens(userProfile.getAllergens())
				.dietGoal(userProfile.getDietGoal())
				.dislikedIngredients(userProfile.getDislikedIngredients())
				.profileImageUrl(userProfile.getProfileImageUrl())
				.name(userProfile.getName())
				.phoneNumber(userProfile.getPhoneNumber())
				.birth(userProfile.getBirth())
				.zipCode(userProfile.getZipCode())
				.address(userProfile.getAddress())
				.addressDetail(userProfile.getAddressDetail())
				.build();
		} else {
			return UserProfileEntity.builder()
				.id(userProfile.getId())
				.userId(userProfile.getUserId())
				.displayName(userProfile.getDisplayName())
				.allergens(userProfile.getAllergens())
				.dietGoal(userProfile.getDietGoal())
				.dislikedIngredients(userProfile.getDislikedIngredients())
				.profileImageUrl(userProfile.getProfileImageUrl())
				.name(userProfile.getName())
				.phoneNumber(userProfile.getPhoneNumber())
				.birth(userProfile.getBirth())
				.zipCode(userProfile.getZipCode())
				.address(userProfile.getAddress())
				.addressDetail(userProfile.getAddressDetail())
				.build();
		}
	}

	public static UserProfile toDomain(UserProfileEntity entity) {
		return UserProfile.create(entity.getId(), entity.getUserId(), entity.getDisplayName(), entity.getAllergens(), entity.getDietGoal(), entity.getDislikedIngredients(),
			entity.getProfileImageUrl(), entity.getName(), entity.getPhoneNumber(), entity.getBirth(), entity.getZipCode(), entity.getAddress(), entity.getAddressDetail());
	}
}
