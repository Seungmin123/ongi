package com.ongi.api.user.adapter.out.persistence;

import com.ongi.api.common.persistence.entity.BaseTimeEntity;
import com.ongi.user.domain.enums.UserTier;
import com.ongi.user.domain.enums.UserTypeEnum;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "user")
public class UserEntity extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "user_id", nullable = false)
	private Long id;

	@Column(name = "email", nullable = false)
	private String email;

	@Column(name = "password_hash", nullable = false)
	private String passwordHash;

	@Enumerated(EnumType.STRING)
	@Column(name = "type", nullable = false, length = 50)
	private UserTypeEnum type;

	@Enumerated(EnumType.STRING)
	@Column(name = "tier", nullable = false, length = 50)
	private UserTier tier;

	@Column(name = "phone_number")
	private String phoneNumber;

	@Column(name = "push_token")
	private String pushToken;

	@Column(name = "marketing_agreed", nullable = false)
	private boolean marketingAgreed;

	@Builder
	public UserEntity(
		Long id,
		String email,
		String passwordHash,
		UserTypeEnum type,
		UserTier tier,
		String phoneNumber,
		String pushToken,
		boolean marketingAgreed
	) {
		this.id = id;
		this.email = email;
		this.passwordHash = passwordHash;
		this.type = type;
		this.tier = tier;
		this.phoneNumber = phoneNumber;
		this.pushToken = pushToken;
		this.marketingAgreed = marketingAgreed;
	}

}
