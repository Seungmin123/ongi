package com.ongi.api.user.adapter.out.persistence;

import com.ongi.user.domain.User;
import com.ongi.user.domain.UserAllergen;
import com.ongi.user.domain.UserDislikedIngredient;
import com.ongi.user.domain.UserProfile;
import com.ongi.user.domain.UserStats;

public class UserMapper {

	public static UserEntity toEntity(User domain) {
		if(domain.getId() == null) {
			return UserEntity.builder()
				.email(domain.getEmail())
				.passwordHash(domain.getPasswordHash())
				.type(domain.getType())
				.tier(domain.getTier())
				.build();
		} else {
			return UserEntity.builder()
				.id(domain.getId())
				.email(domain.getEmail())
				.passwordHash(domain.getPasswordHash())
				.type(domain.getType())
				.tier(domain.getTier())
				.build();
		}
	}

	public static User toDomain(UserEntity entity) {
		return User.create(entity.getId(), entity.getEmail(), entity.getPasswordHash(), entity.getType(), entity.getTier());
	}

	public static UserProfileEntity toEntity(UserProfile domain) {
		if(domain.getId() == null) {
			return UserProfileEntity.builder()
				.userId(domain.getUserId())
				.displayName(domain.getDisplayName())
				.dietGoal(domain.getDietGoal())
				.profileImageUrl(domain.getProfileImageUrl())
				.name(domain.getName())
				.phoneNumber(domain.getPhoneNumber())
				.birth(domain.getBirth())
				.zipCode(domain.getZipCode())
				.address(domain.getAddress())
				.addressDetail(domain.getAddressDetail())
				.build();
		} else {
			return UserProfileEntity.builder()
				.id(domain.getId())
				.userId(domain.getUserId())
				.displayName(domain.getDisplayName())
				.dietGoal(domain.getDietGoal())
				.profileImageUrl(domain.getProfileImageUrl())
				.name(domain.getName())
				.phoneNumber(domain.getPhoneNumber())
				.birth(domain.getBirth())
				.zipCode(domain.getZipCode())
				.address(domain.getAddress())
				.addressDetail(domain.getAddressDetail())
				.build();
		}
	}

	public static UserProfile toDomain(UserProfileEntity entity) {
		return UserProfile.create(entity.getId(), entity.getUserId(), entity.getDisplayName(), entity.getDietGoal(), entity.getProfileImageUrl(),
			entity.getName(), entity.getPhoneNumber(), entity.getBirth(), entity.getZipCode(), entity.getAddress(), entity.getAddressDetail());
	}

	public static UserStatsEntity toEntity(UserStats domain) {
		return UserStatsEntity.builder()
			.userId(domain.getUserId())
			.uploadedRecipeCount(domain.getUploadedRecipeCount())
			.savedRecipeCount(domain.getSavedRecipeCount())
			.myRecipeTotalViewCount(domain.getMyRecipeTotalViewCount())
			.myPostCount(domain.getMyPostCount())
			.myCommentCount(domain.getMyCommentCount())
			.build();
	}

	public static UserStats toDomain(UserStatsEntity entity) {
		return UserStats.create(entity.getId(), entity.getUploadedRecipeCount(), entity.getSavedRecipeCount(), entity.getMyRecipeTotalViewCount(), entity.getMyPostCount(), entity.getMyCommentCount());
	}

	public static UserAllergenEntity toEntity(UserAllergen domain) {
		return UserAllergenEntity.builder()
			.userId(domain.getUserId())
			.allergenGroupId(domain.getAllergenGroupId())
			.build();
	}

	public static UserAllergen toDomain(UserAllergenEntity entity) {
		return UserAllergen.create(entity.getId(), entity.getUserId(), entity.getAllergenGroupId());
	}

	public static UserDislikedIngredientEntity toEntity(UserDislikedIngredient domain) {
		return UserDislikedIngredientEntity.builder()
			.userId(domain.getUserId())
			.ingredientId(domain.getIngredientId())
			.build();
	}

	public static UserDislikedIngredient toDomain(UserDislikedIngredientEntity entity) {
		return UserDislikedIngredient.create(entity.getId(), entity.getUserId(), entity.getIngredientId());
	}
}
