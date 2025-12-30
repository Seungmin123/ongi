package com.ongi.api.user.adapter.out.persistence;

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
@Table(name="user_allergen",
	uniqueConstraints = @UniqueConstraint(
		name="uk_user_allergen_group", columnNames={"user_id","allergen_group_id"}),
	indexes = {
		@Index(name="idx_user_allergen_user", columnList="user_id, allergen_group_id"),
		@Index(name="idx_user_allergen_group", columnList="allergen_group_id, user_id")
	}
)
public class UserAllergenEntity {

	@Id
	@GeneratedValue(strategy= GenerationType.IDENTITY)
	@Column(name="user_allergen_id")
	private Long id;

	@Column(name="user_id", nullable=false)
	private Long userId;

	@Column(name="allergen_group_id", nullable=false)
	private Long allergenGroupId;

	@Builder
	public UserAllergenEntity(Long id, Long userId, Long allergenGroupId) {
		this.id = id;
		this.userId = userId;
		this.allergenGroupId = allergenGroupId;
	}
}
