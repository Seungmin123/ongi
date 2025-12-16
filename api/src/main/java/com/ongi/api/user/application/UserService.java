package com.ongi.api.user.application;

import com.ongi.api.user.persistence.MyPageQueryAdapter;
import com.ongi.api.user.persistence.UserAdapter;
import com.ongi.api.user.web.dto.MyPageResponse;
import com.ongi.api.user.web.dto.MyPageResponse.Basic;
import com.ongi.api.user.web.dto.MyPageResponse.Personalization;
import com.ongi.api.user.web.dto.MyPageResponse.Summary;
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
}
