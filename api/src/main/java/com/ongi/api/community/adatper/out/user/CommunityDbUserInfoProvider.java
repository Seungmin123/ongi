package com.ongi.api.community.adatper.out.user;

import com.ongi.api.user.adapter.out.persistence.UserAdapter;
import com.ongi.user.domain.UserProfile;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CommunityDbUserInfoProvider implements UserInfoProvider {

	private final UserAdapter userAdapter;

	@Override
	public Map<Long, UserSummary> getUserSummaries(Set<Long> userIds) {
		if (userIds == null || userIds.isEmpty()) return Map.of();

		// 중복 제거 + 너무 큰 IN 방지(필요 시 청크)
		Set<Long> ids = userIds.stream()
			.filter(Objects::nonNull)
			.collect(Collectors.toSet());

		return userAdapter.findUserProfilesByIdIn(ids).stream()
			.collect(java.util.stream.Collectors.toMap(
					UserProfile::getId,
					u -> new UserSummary(u.getId(), u.getDisplayName(), u.getProfileImageUrl())
			));
	}
}
