package com.ongi.user.domain;

import com.ongi.user.domain.enums.UserTypeEnum;

public class User {

	private Long id;

	private String email;

	private String passwordHash;

	private UserTypeEnum type;

	private User(
		Long id,
		String email,
		String passwordHash,
		UserTypeEnum type
	) {
		this.id = id;
		this.email = email;
		this.passwordHash = passwordHash;
		this.type = type;
	}

	public static User create(
		Long id, String email, String passwordHash, UserTypeEnum type
	) {
		return new User(id, email, passwordHash, type);
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPasswordHash() {
		return passwordHash;
	}

	public void setPasswordHash(String passwordHash) {
		this.passwordHash = passwordHash;
	}

	public UserTypeEnum getType() {
		return type;
	}

	public void setType(UserTypeEnum type) {
		this.type = type;
	}
}
