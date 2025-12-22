package com.ongi.api.user.application.command;

import com.ongi.api.user.adapter.out.cache.persistence.MyPageQueryAdapter;
import com.ongi.api.user.adapter.out.cache.persistence.UserAdapter;
import com.ongi.api.user.web.dto.MyPageBasicUpdateRequest;
import com.ongi.api.user.web.dto.MyPagePersonalizationUpdateRequest;
import com.ongi.api.user.web.dto.MyPageResponse;
import com.ongi.api.user.web.dto.MyPageResponse.Basic;
import com.ongi.api.user.web.dto.MyPageResponse.Personalization;
import com.ongi.api.user.web.dto.MyPageResponse.Summary;
import com.ongi.api.user.web.dto.MyPageStatsResponse;
import com.ongi.api.user.web.dto.MyPageSummaryUpdateRequest;
import com.ongi.user.domain.enums.MeInclude;
import java.util.List;
import java.util.Set;
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
			var row = myPageQueryAdapter.findMePersonalization(userId);
			personalization = new MyPageResponse.Personalization(
				readList(row.allergens()),
				row.dietGoal(),
				readList(row.dislikedIngredients())
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
		var userProfile = userAdapter.findUserProfileByUserId(userId).orElseThrow(() -> new IllegalStateException("User not found"));

		userProfile.setAllergens(writeList(request.allergens()));
		userProfile.setDietGoal(request.dietGoal());
		userProfile.setDislikedIngredients(writeList(request.dislikedIngredients()));
	}

	@Transactional(transactionManager = "transactionManager", readOnly = true)
	public MyPageStatsResponse getMyPageStats(Long userId) {
		var userStats = userAdapter.findUserStatsById(userId).orElseThrow(() -> new IllegalStateException("User not found"));
		return new MyPageStatsResponse(userStats.getUploadedRecipeCount(), userStats.getSavedRecipeCount(),
			userStats.getMyRecipeTotalViewCount(), userStats.getMyPostCount(), userStats.getMyCommentCount());
	}
}
