package com.ongi.api.user.adapter.out.persistence.repository;

import com.ongi.api.user.adapter.out.persistence.UserDislikedIngredientEntity;
import com.ongi.api.user.adapter.out.persistence.projection.DislikedIngredientsRow;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserDislikedIngredientRepository extends JpaRepository<UserDislikedIngredientEntity, UUID> {

	@Query("""
	    select new com.ongi.api.user.adapter.out.persistence.projection.DislikedIngredientsRow(i.id, i.code, i.name, i.category)
	    from UserDislikedIngredientEntity ud
	    join IngredientEntity i on i.id = ud.ingredientId
	    where ud.userId = :userId
	  """)
	List<DislikedIngredientsRow> findDislikedIngredientRowByUserId(Long userId);

	@Query("select ud.ingredientId from UserDislikedIngredientEntity ud where ud.userId = :userId")
	Set<Long> findDislikedIngredientsByUserId(@Param("userId") Long userId);

	@Modifying
	@Query("delete from UserDislikedIngredientEntity ud where ud.userId = :userId and ud.ingredientId in :ids")
	int deleteDislikedIngredientByUserIdAndIngredientIdIn(@Param("userId") Long userId, @Param("ids") Set<Long> ids);

}
