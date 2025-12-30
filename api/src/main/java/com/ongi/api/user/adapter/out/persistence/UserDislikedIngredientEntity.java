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
	name="user_disliked_ingredient",
	uniqueConstraints = @UniqueConstraint(
		name="uk_user_disliked_ingredient",
		columnNames={"user_id", "ingredient_id"}
	),
	indexes = {
		@Index(name="idx_user_disliked_user", columnList="user_id, ingredient_id"),
		@Index(name="idx_user_disliked_ing", columnList="ingredient_id, user_id")
	}
)
public class UserDislikedIngredientEntity extends BaseTimeEntity {

	@Id @GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name="user_disliked_ingredient_id")
	private Long id;

	@Column(name="user_id", nullable=false)
	private Long userId;

	@Column(name="ingredient_id", nullable=false)
	private Long ingredientId;

	@Builder
	public UserDislikedIngredientEntity(Long id, Long userId, Long ingredientId) {
		this.id = id;
		this.userId = userId;
		this.ingredientId = ingredientId;
	}
}
