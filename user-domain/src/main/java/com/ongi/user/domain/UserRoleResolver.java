package com.ongi.user.domain;

import com.ongi.user.domain.enums.UserRole;

public final class UserRoleResolver {

	public static String[] getRoles(User user) {
		return switch (user.getTier()) {
			case GUEST ->
				new String[]{UserRole.GUEST.getKey()};
			case USER, GOLD, PLATINUM, MASTER, CHALLENGER ->
				new String[]{UserRole.USER.getKey()};
			case ENGINEER ->
				new String[]{
					UserRole.USER.getKey(),
					UserRole.ENGINEER.getKey()
				};
		};
	}

	private UserRoleResolver() {}
}
