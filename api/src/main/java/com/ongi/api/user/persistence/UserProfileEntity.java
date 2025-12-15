package com.ongi.api.user.persistence;

import com.ongi.api.common.persistence.entity.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "user_profile")
public class UserProfileEntity extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "user_profile_id", nullable = false)
	private Long id;

	@Column(name = "user_id", nullable = false)
	private Long userId;

	@Column(name = "display_name")
	private String displayName;

	// TODO List
	@Column(name = "allergens")
	private String allergens;

	@Column(name = "diet_goal")
	private Integer dietGoal;

	// TODO List
	@Column(name = "disliked_ingredients")
	private String dislikedIngredients;

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
		String allergens,
		Integer dietGoal,
		String dislikedIngredients,
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
		this.allergens = allergens;
		this.dietGoal = dietGoal;
		this.dislikedIngredients = dislikedIngredients;
		this.profileImageUrl = profileImageUrl;
		this.name = name;
		this.phoneNumber = phoneNumber;
		this.birth = birth;
		this.zipCode = zipCode;
		this.address = address;
		this.addressDetail = addressDetail;
	}

}
