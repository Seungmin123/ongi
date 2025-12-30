package com.ongi.api.ingredients.adapter.out.persistence.repository;

import com.ongi.api.ingredients.adapter.out.persistence.NutritionEntity;
import com.ongi.ingredients.domain.enums.NutritionEnum;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NutritionRepository extends JpaRepository<NutritionEntity, Long> {

	Optional<NutritionEntity> findByCode(NutritionEnum code);
}
