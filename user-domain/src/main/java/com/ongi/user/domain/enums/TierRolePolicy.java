package com.ongi.user.domain.enums;

import java.util.List;
import java.util.Map;

public final class TierRolePolicy {

	private static final Map<UserTier, List<UserRole>> MAP = Map.of(
		UserTier.GUEST, List.of(UserRole.USER),
		UserTier.USER, List.of(UserRole.USER),
		UserTier.GOLD, List.of(UserRole.USER),
		UserTier.PLATINUM, List.of(UserRole.USER),
		UserTier.MASTER, List.of(UserRole.USER),
		UserTier.CHALLENGER, List.of(UserRole.USER),
		UserTier.ENGINEER, List.of(UserRole.USER, UserRole.ENGINEER)
	);

	public static List<UserRole> rolesOf(UserTier tier) {
		return MAP.getOrDefault(tier, List.of());
	}

	private TierRolePolicy() {}
}
