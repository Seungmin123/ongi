package com.ongi.api.user.adapter.out.persistence.repository;

import com.ongi.api.user.adapter.out.persistence.UserAllergenEntity;
import com.ongi.api.user.adapter.out.persistence.projection.AllergensGroupRow;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserAllergenRepository extends JpaRepository<UserAllergenEntity, UUID> {

	@Query("""
	    select new com.ongi.api.user.adapter.out.persistence.projection.AllergensGroupRow(g.id, g.code, g.nameKo)
	    from UserAllergenEntity ua
	    join AllergenGroupEntity g on g.id = ua.allergenGroupId
	    where ua.userId = :userId
	  """)
	List<AllergensGroupRow> findUserAllergenRowByUserId(Long userId);

	@Query("select ua.allergenGroupId from UserAllergenEntity ua where ua.userId = :userId")
	Set<Long> findUserAllergenIdsByUserId(@Param("userId") Long userId);

	@Modifying
	@Query("delete from UserAllergenEntity ua where ua.userId = :userId and ua.allergenGroupId in :ids")
	int deleteUserAllergenByUserIdAndAllergenGroupIdIn(@Param("userId") Long userId, @Param("ids") Set<Long> ids);

}
