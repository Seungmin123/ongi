package com.ongi.api.user.adapter.out.persistence;

import com.ongi.api.user.adapter.out.persistence.projection.MeBasicRow;
import com.ongi.api.user.adapter.out.persistence.projection.MeSummaryRow;
import com.ongi.api.user.adapter.out.persistence.repository.UserAllergenRepository;
import com.ongi.api.user.adapter.out.persistence.repository.UserDislikedIngredientRepository;
import com.ongi.api.user.adapter.out.persistence.repository.UserProfileRepository;
import com.ongi.api.user.adapter.out.persistence.repository.UserRepository;
import com.ongi.api.user.adapter.out.persistence.projection.AllergensGroupRow;
import com.ongi.api.user.adapter.out.persistence.projection.DislikedIngredientsRow;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@Repository
public class MyPageQueryAdapter {

	private final UserRepository userRepository;

	private final UserProfileRepository userProfileRepository;

	private final UserAllergenRepository userAllergenRepository;

	private final UserDislikedIngredientRepository userDislikedIngredientRepository;

	public String findUserEmailByUserId(Long userId) {
		return userRepository.findEmailById(userId);
	}

	public MeSummaryRow findMeSummary(Long userId, String email) {
		return userProfileRepository.findMeSummary(userId, email);
	}

	public MeBasicRow findMeBasic(Long userId) {
		return userProfileRepository.findMeBasic(userId);
	}

	int updateSummary(Long userId, String displayName, String profileImageUrl) {
		return userProfileRepository.updateSummary(userId, displayName, profileImageUrl);
	}

	int updateBasic(Long userId, String name, String birth, String zipCode, String address, String addressDetail) {
		return userProfileRepository.updateBasic(userId, name, birth, zipCode, address, addressDetail);
	}

	int updatePersonalization(Long userId, String allergens, Double dietGoal, String dislikedIngredients) {
		// TODO Allergen, Dislike
		return userProfileRepository.updatePersonalization(userId, dietGoal);
	}

	public List<DislikedIngredientsRow> findDislikedIngredientRowByUserId(Long userId) {
		return userDislikedIngredientRepository.findDislikedIngredientRowByUserId(userId);
	}

	public Set<Long> findUserDislikedIngredientIdsByUserId(Long userId) {
		return userDislikedIngredientRepository.findDislikedIngredientsByUserId(userId);
	}

	public int deleteDislikedIngredientByUserIdAndIngredientIdIn(Long userId, Set<Long> ingredientIds) {
		return userDislikedIngredientRepository.deleteDislikedIngredientByUserIdAndIngredientIdIn(userId, ingredientIds);
	}

	public List<AllergensGroupRow> findUserAllergenRowByUserId(Long userId) {
		return userAllergenRepository.findUserAllergenRowByUserId(userId);
	}

	public Set<Long> findUserAllergenIdsByUserId(Long userId) {
		return userAllergenRepository.findUserAllergenIdsByUserId(userId);
	}

	public int deleteUserAllergenByUserIdAndAllergenGroupIdIn(Long userId, Set<Long> allergensGroupIds) {
		return userAllergenRepository.deleteUserAllergenByUserIdAndAllergenGroupIdIn(userId, allergensGroupIds);
	}
}
