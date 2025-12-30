package com.ongi.api.user.adapter.out.persistence;

import com.ongi.api.common.persistence.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(
	name = "user_profile",
	uniqueConstraints = @UniqueConstraint(name="uk_user_profile_user_id", columnNames = "user_id"),
	indexes = @Index(name="idx_user_profile_user_id", columnList="user_id"))
public class UserProfileEntity extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "user_profile_id", nullable = false)
	private Long id;

	@Column(name = "user_id", nullable = false)
	private Long userId;

	@Column(name = "display_name")
	private String displayName;

	@Column(name = "diet_goal")
	private Double dietGoal;

	@Column(name = "profile_image_url")
	private String profileImageUrl;

	@Column(name = "name")
	private String name;

	@Column(name = "phone_number")
	private String phoneNumber;

	@Column(name = "birth", comment = "YYYYMMDD")
	private String birth;

	@Column(name = "zip_code")
	private String zipCode;

	@Column(name = "address")
	private String address;

	@Column(name = "address_detail")
	private String addressDetail;

	@Builder
	public UserProfileEntity(
		Long id,
		Long userId,
		String displayName,
		Double dietGoal,
		String profileImageUrl,
		String name,
		String phoneNumber,
		String birth,
		String zipCode,
		String address,
		String addressDetail
	) {
		this.id = id;
		this.userId = userId;
		this.displayName = displayName;
		this.dietGoal = dietGoal;
		this.profileImageUrl = profileImageUrl;
		this.name = name;
		this.phoneNumber = phoneNumber;
		this.birth = birth;
		this.zipCode = zipCode;
		this.address = address;
		this.addressDetail = addressDetail;
	}

}
