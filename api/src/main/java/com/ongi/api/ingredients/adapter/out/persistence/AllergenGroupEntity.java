package com.ongi.api.ingredients.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name="allergen_group",
	uniqueConstraints = @UniqueConstraint(name="uk_allergen_group_code", columnNames="code"))
public class AllergenGroupEntity {
	@Id
	@GeneratedValue(strategy= GenerationType.IDENTITY)
	@Column(name="allergen_group_id")
	private Long id;

	@Column(name="code", nullable=false, length=50)
	private String code;

	@Column(name="name_ko", nullable=false, length=100)
	private String nameKo;

	@Builder
	public AllergenGroupEntity(Long id, String code, String nameKo) {
		this.id = id;
		this.code = code;
		this.nameKo = nameKo;
	}
}
