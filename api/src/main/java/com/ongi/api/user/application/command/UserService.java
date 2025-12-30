package com.ongi.api.user.application.command;

import com.ongi.api.user.adapter.out.persistence.MyPageQueryAdapter;
import com.ongi.api.user.adapter.out.persistence.UserAdapter;
import com.ongi.api.user.port.IngredientsProvider;
import com.ongi.api.user.web.dto.MyPageBasicUpdateRequest;
import com.ongi.api.user.web.dto.MyPagePersonalizationUpdateRequest;
import com.ongi.api.user.web.dto.MyPageResponse;
import com.ongi.api.user.web.dto.MyPageResponse.Basic;
import com.ongi.api.user.web.dto.MyPageResponse.Personalization;
import com.ongi.api.user.web.dto.MyPageResponse.Summary;
import com.ongi.api.user.web.dto.MyPageStatsResponse;
import com.ongi.api.user.web.dto.MyPageSummaryUpdateRequest;
import com.ongi.ingredients.domain.AllergenGroup;
import com.ongi.ingredients.domain.Ingredient;
import com.ongi.user.domain.enums.MeInclude;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

@RequiredArgsConstructor
@Service
public class UserService {

	private final FileService fileService;

	private final UserAdapter userAdapter;

	private final MyPageQueryAdapter myPageQueryAdapter;

	private final IngredientsProvider ingredientsProvider;

	private final ObjectMapper objectMapper;

	@Transactional(transactionManager = "transactionManager", readOnly = true)
	public MyPageResponse getMe(Long userId, Set<MeInclude> includes) {
		var profile = userAdapter.findUserProfileByUserId(userId)
			.orElseThrow();

		Summary summary = null;
		Basic basic = null;
		Personalization personalization = null;

		if (includes.contains(MeInclude.SUMMARY)) {
			String email = myPageQueryAdapter.findUserEmailByUserId(userId);
			var row = myPageQueryAdapter.findMeSummary(userId, email);
			summary = new MyPageResponse.Summary(row.email(), row.displayName(), row.profileImageUrl());
		}

		if (includes.contains(MeInclude.BASIC)) {
			var row = myPageQueryAdapter.findMeBasic(userId);
			basic = new MyPageResponse.Basic(row.name(), row.birth(), row.zipCode(), row.address(), row.addressDetail());
		}

		if (includes.contains(MeInclude.PERSONALIZATION)) {
			var userProfile = userAdapter.findUserProfileByUserId(userId).orElseThrow(() -> new IllegalStateException("User not found"));
			var userAllergens = myPageQueryAdapter.findUserAllergenRowByUserId(userId);
			var userDislikedIngredients = myPageQueryAdapter.findDislikedIngredientRowByUserId(userId);

			personalization = new MyPageResponse.Personalization(
				userProfile.getDietGoal(),
				userAllergens,
				userDislikedIngredients
			);
		}

		return new MyPageResponse(summary, basic, personalization);
	}

	private List<String> readList(String json) {
		if (json == null || json.isBlank()) return List.of();
		try {
			return objectMapper.readValue(json, new TypeReference<>() {});
		} catch (Exception e) {
			return List.of();
		}
	}

	protected String writeList(List<String> list) {
		if (list == null) return null;
		try {
			return objectMapper.writeValueAsString(list);
		} catch (Exception e) {
			return "";
		}
	}

	@Transactional(transactionManager = "transactionManager")
	public void myPageSummaryUpdate(Long userId, MyPageSummaryUpdateRequest request) {
		// TODO Custom Exception
		var userProfile = userAdapter.findUserProfileByUserId(userId).orElseThrow(() -> new IllegalStateException("User not found"));

		if (request.displayName() != null) userProfile.setDisplayName(request.displayName().trim());

		String finalProfileImageKey = null;
		if (request.profileImageUploadToken() != null && request.profileImageObjectKey() != null) {
			finalProfileImageKey = fileService.consumeAndPromoteProfileImage(
				request.profileImageUploadToken(),  // UUID
				request.profileImageObjectKey(),    // tmp objectKey
				userId
			);

			userProfile.setProfileImageUrl(finalProfileImageKey);
		}

		userAdapter.save(userProfile);
	}

	@Transactional(transactionManager = "transactionManager")
	public void myPageBasicUpdate(Long userId, MyPageBasicUpdateRequest request) {
		var userProfile = userAdapter.findUserProfileByUserId(userId).orElseThrow(() -> new IllegalStateException("User not found"));

		if (request.name() != null) userProfile.setName(request.name().trim());
		if (request.birth() != null) userProfile.setBirth(request.birth());
		if (request.zipCode() != null) userProfile.setZipCode(request.zipCode().trim());
		if (request.address() != null) userProfile.setAddress(request.address().trim());
		if (request.addressDetail() != null) userProfile.setAddressDetail(request.addressDetail().trim());

		userAdapter.save(userProfile);
	}

	@Transactional(transactionManager = "transactionManager")
	public void myPagePersonalizationUpdate(Long userId, MyPagePersonalizationUpdateRequest request) {
		updateDietGoal(userId, request.dietGoal());

		Set<Long> newAllergenIds = new LinkedHashSet<>(request.allergenGroupIds());
		updateUserAllergens(userId, newAllergenIds);

		Set<Long> newDislikedIds = new LinkedHashSet<>(request.dislikedIngredientIds());
		updateUserDisliked(userId, newDislikedIds);
	}

	@Transactional(transactionManager = "transactionManager")
	public void updateDietGoal(Long userId, Double dietGoal) {
		var userProfile = userAdapter.findUserProfileByUserId(userId).orElseThrow(() -> new IllegalStateException("User not found"));
		userProfile.setDietGoal(dietGoal);
	}

	@Transactional(transactionManager = "transactionManager")
	public void updateUserAllergens(Long userId, Set<Long> allergensGroupIds) {
		validateAllergenGroupsExist(allergensGroupIds);

		Set<Long> curAllergenIds = myPageQueryAdapter.findUserAllergenIdsByUserId(userId);

		Set<Long> toDelAllergen = diff(curAllergenIds, allergensGroupIds);
		Set<Long> toAddAllergen = diff(allergensGroupIds, curAllergenIds);

		// delete 먼저 (unique 충돌 방지)
		if (!toDelAllergen.isEmpty()) {
			myPageQueryAdapter.deleteUserAllergenByUserIdAndAllergenGroupIdIn(userId, toDelAllergen);
		}

		if (!toAddAllergen.isEmpty()) {
			userAdapter.saveAllUserAllergen(userId, toAddAllergen);
		}
	}

	@Transactional(transactionManager = "transactionManager")
	public void updateUserDisliked(Long userId, Set<Long> dislikedIngredientIds) {
		validateIngredientsExist(dislikedIngredientIds);

		Set<Long> curDislikedIngredientIds = myPageQueryAdapter.findUserDislikedIngredientIdsByUserId(userId);

		Set<Long> toDelDislikedIngredientsIds = diff(curDislikedIngredientIds, dislikedIngredientIds);
		Set<Long> toAddDislikedIngredientsIds = diff(dislikedIngredientIds, curDislikedIngredientIds);

		if (!toDelDislikedIngredientsIds.isEmpty()) {
			myPageQueryAdapter.deleteDislikedIngredientByUserIdAndIngredientIdIn(userId, toDelDislikedIngredientsIds);
		}

		if(!toAddDislikedIngredientsIds.isEmpty()) {
			userAdapter.saveAllDislikedIngredients(userId, toAddDislikedIngredientsIds);
		}
	}

	private Set<Long> diff(Set<Long> a, Set<Long> b) {
		Set<Long> r = new HashSet<>(a);
		r.removeAll(b);
		return r;
	}

	private void validateAllergenGroupsExist(Set<Long> ids) {
		if (ids.isEmpty()) return;

		Set<Long> existsIds = ingredientsProvider.findAllergenGroupsByIds(ids).stream()
			.map(AllergenGroup::getId)
			.collect(Collectors.toSet());

		if (existsIds.size() != ids.size()) {
			Set<Long> missing = new HashSet<>(ids);
			missing.removeAll(existsIds);
			throw new IllegalArgumentException("Unknown allergenGroupIds: " + missing);
		}
	}

	private void validateIngredientsExist(Set<Long> ids) {
		if (ids.isEmpty()) return;

		Set<Long> existsIds = ingredientsProvider.findIngredientsByIds(ids).stream()
			.map(Ingredient::getIngredientId)
			.collect(Collectors.toSet());

		if (existsIds.size() != ids.size()) {
			Set<Long> missing = new HashSet<>(ids);
			missing.removeAll(existsIds);
			throw new IllegalArgumentException("Unknown IngredientsIds: " + missing);
		}
	}

	@Transactional(transactionManager = "transactionManager", readOnly = true)
	public MyPageStatsResponse getMyPageStats(Long userId) {
		var userStats = userAdapter.findUserStatsById(userId).orElseThrow(() -> new IllegalStateException("User not found"));
		return new MyPageStatsResponse(userStats.getUploadedRecipeCount(), userStats.getSavedRecipeCount(),
			userStats.getMyRecipeTotalViewCount(), userStats.getMyPostCount(), userStats.getMyCommentCount());
	}
}
