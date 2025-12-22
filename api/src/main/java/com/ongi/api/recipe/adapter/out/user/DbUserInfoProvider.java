package com.ongi.api.recipe.adapter.out.user;

import com.ongi.api.recipe.port.UserInfoProvider;
import com.ongi.api.recipe.web.dto.UserSummary;
import com.ongi.api.user.adapter.out.persistence.UserAdapter;
import com.ongi.user.domain.UserProfile;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DbUserInfoProvider implements UserInfoProvider {

	private final UserAdapter userAdapter;

	@Override
	public Map<Long, UserSummary> getUsersByIds(Set<Long> userIds) {
		if (userIds == null || userIds.isEmpty()) return Map.of();

		return userAdapter.findUserProfilesByIdIn(userIds).stream()
			.collect(java.util.stream.Collectors.toMap(
				UserProfile::getId,
				u -> new UserSummary(u.getId(), u.getDisplayName(), u.getProfileImageUrl())
			));
	}
}
