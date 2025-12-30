package com.ongi.api.ingredients.adapter.out.persistence.repository;

import com.ongi.api.ingredients.adapter.out.persistence.IngredientEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IngredientRepository extends JpaRepository<IngredientEntity, Long> {

	Optional<IngredientEntity> findFirstByNameOrderByNameAsc(String name);

	Optional<IngredientEntity> findByName(String name);

}
