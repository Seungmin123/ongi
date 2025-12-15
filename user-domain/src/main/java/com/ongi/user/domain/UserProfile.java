package com.ongi.user.domain;

public class UserProfile {

	private Long id;

	private Long userId;

	private String displayName;

	// TODO List
	private String allergens;

	private Integer dietGoal;

	// TODO List
	private String dislikedIngredients;

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

	public static UserProfile create(
		Long id, Long userId, String displayName, String allergens, Integer dietGoal, String dislikedIngredients,
		String profileImageUrl, String name, String phoneNumber, String birth, String zipCode, String address, String addressDetail
	) {
		return new UserProfile(id, userId, displayName, allergens, dietGoal, dislikedIngredients, profileImageUrl, name, phoneNumber, birth, zipCode, address, addressDetail);
	}

	public static UserProfile create(
		Long userId, String displayName, String profileImageUrl
	) {
		return new UserProfile(null, userId, displayName, null, null, null, profileImageUrl, null, null, null, null, null, null);
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

	public String getAllergens() {
		return allergens;
	}

	public void setAllergens(String allergens) {
		this.allergens = allergens;
	}

	public Integer getDietGoal() {
		return dietGoal;
	}

	public void setDietGoal(Integer dietGoal) {
		this.dietGoal = dietGoal;
	}

	public String getDislikedIngredients() {
		return dislikedIngredients;
	}

	public void setDislikedIngredients(String dislikedIngredients) {
		this.dislikedIngredients = dislikedIngredients;
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
