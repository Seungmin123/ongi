package com.ongi.api.ingredients.adapter.out.persistence.repository;

import com.ongi.api.ingredients.adapter.out.persistence.RecipeRelatedConfigEntity;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface RecipeRelatedConfigRepository extends JpaRepository<RecipeRelatedConfigEntity, Long> {

	@Query("select c from RecipeRelatedConfigEntity c where c.id = 1")
	Optional<RecipeRelatedConfigEntity> findSingleton();

}
