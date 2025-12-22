package com.ongi.api.user.adapter.out.cache.persistence;

import com.ongi.api.user.adapter.out.cache.persistence.projection.MeBasicRow;
import com.ongi.api.user.adapter.out.cache.persistence.projection.MePersonalizationRow;
import com.ongi.api.user.adapter.out.cache.persistence.projection.MeSummaryRow;
import com.ongi.api.user.adapter.out.cache.persistence.repository.UserProfileRepository;
import com.ongi.api.user.adapter.out.cache.persistence.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@Repository
public class MyPageQueryAdapter {

	private final UserRepository userRepository;

	private final UserProfileRepository userProfileRepository;

	public String findUserEmailByUserId(Long userId) {
		return userRepository.findEmailById(userId);
	}

	public MeSummaryRow findMeSummary(Long userId, String email) {
		return userProfileRepository.findMeSummary(userId, email);
	}

	public MeBasicRow findMeBasic(Long userId) {
		return userProfileRepository.findMeBasic(userId);
	}

	public MePersonalizationRow findMePersonalization(Long userId) {
		return userProfileRepository.findMePersonalization(userId);
	}

	int updateSummary(Long userId, String displayName, String profileImageUrl) {
		return userProfileRepository.updateSummary(userId, displayName, profileImageUrl);
	}

	int updateBasic(Long userId, String name, String birth, String zipCode, String address, String addressDetail) {
		return userProfileRepository.updateBasic(userId, name, birth, zipCode, address, addressDetail);
	}

	int updatePersonalization(Long userId, String allergens, Double dietGoal, String dislikedIngredients) {
		return userProfileRepository.updatePersonalization(userId, allergens, dietGoal, dislikedIngredients);
	}


}
