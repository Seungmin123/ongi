package com.ongi.api.user.application;

import com.ongi.api.user.persistence.UserAdapter;
import com.ongi.api.user.web.dto.MemberJoinRequest;
import com.ongi.api.user.web.dto.MemberResponse;
import com.ongi.user.domain.User;
import com.ongi.user.domain.UserProfile;
import com.ongi.user.domain.enums.UserTier;
import com.ongi.user.domain.enums.UserTypeEnum;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class UserService {

	private final UserAdapter userAdapter;

	private final PasswordEncoder passwordEncoder;

	@Transactional(transactionManager = "transactionManager")
	public void join(MemberJoinRequest request) {
		User user = User.create(null, request.email(), passwordEncoder.encode(request.password()), UserTypeEnum.EMAIL, UserTier.USER);
		user = userAdapter.save(user);

		UserProfile userProfile = UserProfile.create(user.getId(), request.displayName(), request.profileImageUrl());
		userAdapter.save(userProfile);
	}

}
