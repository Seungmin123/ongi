package com.ongi.api.ingredients.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "ingredient_idf")
public class IngredientIdfEntity {

	@Id
	@Column(name = "ingredient_id", nullable = false)
	private Long ingredientId;

	@Column(name = "df", nullable = false, comment = "document frequency - 이 재료를 쓰는 레시피 수")
	private Long df;

	@Column(name = "idf", nullable = false, comment = "log((N+1)/(df+1)) + 1 같은 값")
	private Double idf;

	@Column(name = "computed_at", nullable = false)
	private LocalDateTime computedAt;

}
