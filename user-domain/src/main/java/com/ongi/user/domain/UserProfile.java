package com.ongi.user.domain;

public class UserProfile {

	private Long id;

	private Long userId;

	private String displayName;

	private Double dietGoal;

	private String profileImageUrl;

	private String name;

	private String phoneNumber;

	private String birth;

	private String zipCode;

	private String address;

	private String addressDetail;

	private UserProfile(
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

	public static UserProfile create(
		Long id, Long userId, String displayName, Double dietGoal, String profileImageUrl,
		String name, String phoneNumber, String birth, String zipCode, String address, String addressDetail
	) {
		return new UserProfile(id, userId, displayName, dietGoal, profileImageUrl, name, phoneNumber, birth, zipCode, address, addressDetail);
	}

	public static UserProfile create(
		Long userId, String displayName, String profileImageUrl
	) {
		return new UserProfile(null, userId, displayName, null,   profileImageUrl, null, null, null, null, null, null);
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public Double getDietGoal() {
		return dietGoal;
	}

	public void setDietGoal(Double dietGoal) {
		this.dietGoal = dietGoal;
	}

	public String getProfileImageUrl() {
		return profileImageUrl;
	}

	public void setProfileImageUrl(String profileImageUrl) {
		this.profileImageUrl = profileImageUrl;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPhoneNumber() {
		return phoneNumber;
	}

	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}

	public String getBirth() {
		return birth;
	}

	public void setBirth(String birth) {
		this.birth = birth;
	}

	public String getZipCode() {
		return zipCode;
	}

	public void setZipCode(String zipCode) {
		this.zipCode = zipCode;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getAddressDetail() {
		return addressDetail;
	}

	public void setAddressDetail(String addressDetail) {
		this.addressDetail = addressDetail;
	}
}
