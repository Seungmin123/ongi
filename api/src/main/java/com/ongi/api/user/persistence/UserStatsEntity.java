package com.ongi.api.user.persistence;

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
import org.hibernate.annotations.ColumnDefault;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "user")
public class UserStatsEntity extends BaseTimeEntity {

	@Id
	@Column(name = "user_id", nullable = false)
	private Long id;

	@ColumnDefault("0")
	@Column(name = "uploaded_recipe_count", nullable = false)
	private Long uploadedRecipeCount;

	@ColumnDefault("0")
	@Column(name = "saved_recipe_count", nullable = false)
	private Long savedRecipeCount;

	@ColumnDefault("0")
	@Column(name = "my_recipe_total_view_count", nullable = false)
	private Long myRecipeTotalViewCount;

	@ColumnDefault("0")
	@Column(name = "my_post_count", nullable = false)
	private Long myPostCount;

	@ColumnDefault("0")
	@Column(name = "my_comment_count", nullable = false)
	private Long myCommentCount;

	@Builder
	public UserStatsEntity(
		Long userId,
		Long uploadedRecipeCount,
		Long savedRecipeCount,
		Long myRecipeTotalViewCount,
		Long myPostCount,
		Long myCommentCount
	) {
		this.id = userId;
		this.uploadedRecipeCount = uploadedRecipeCount;
		this.savedRecipeCount = savedRecipeCount;
		this.myRecipeTotalViewCount = myRecipeTotalViewCount;
		this.myPostCount = myPostCount;
		this.myCommentCount = myCommentCount;
	}

}
