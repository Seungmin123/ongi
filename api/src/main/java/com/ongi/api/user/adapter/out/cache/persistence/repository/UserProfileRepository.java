package com.ongi.api.user.adapter.out.cache.persistence.repository;

import com.ongi.api.user.adapter.out.cache.persistence.UserProfileEntity;
import com.ongi.api.user.adapter.out.cache.persistence.projection.MeBasicRow;
import com.ongi.api.user.adapter.out.cache.persistence.projection.MePersonalizationRow;
import com.ongi.api.user.adapter.out.cache.persistence.projection.MeSummaryRow;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserProfileRepository extends JpaRepository<UserProfileEntity, Long> {

	Optional<UserProfileEntity> findByDisplayName(String displayName);

	Optional<UserProfileEntity> findByUserId(Long userId);

	@Query("""
      select new com.ongi.api.user.persistence.projection.MeSummaryRow(
        :email, p.displayName, p.profileImageUrl
      )
      from UserProfileEntity p
      where p.userId = :userId
    """)
	MeSummaryRow findMeSummary(@Param("userId") Long userId, @Param("email") String email);

	@Query("""
      select new com.ongi.api.user.persistence.projection.MeBasicRow(
        p.name, p.birth, p.zipCode, p.address, p.addressDetail
      )
      from UserProfileEntity p
      where p.userId = :userId
    """)
	MeBasicRow findMeBasic(@Param("userId") Long userId);

	@Query("""
      select new com.ongi.api.user.persistence.projection.MePersonalizationRow(
        p.allergens, p.dietGoal, p.dislikedIngredients
      )
      from UserProfileEntity p
      where p.userId = :userId
    """)
	MePersonalizationRow findMePersonalization(@Param("userId") Long userId);

	@Modifying
	@Query("""
	update UserProfileEntity p set
	  p.displayName = coalesce(:displayName, p.name),
	  p.profileImageUrl = coalesce(:profileImageUrl, p.zipCode)
	where p.userId = :userId
	""")
	int updateSummary(Long userId, String displayName, String profileImageUrl);

	@Modifying
	@Query("""
	update UserProfileEntity p set
	  p.name = coalesce(:name, p.name),
	  p.birth = coalesce(:birth, p.birth),
	  p.zipCode = coalesce(:zipCode, p.zipCode),
	  p.address = coalesce(:address, p.address),
	  p.addressDetail = coalesce(:addressDetail, p.addressDetail)
	where p.userId = :userId
	""")
	int updateBasic(Long userId, String name, String birth, String zipCode, String address, String addressDetail);

	@Modifying
	@Query("""
	update UserProfileEntity p set
	  p.allergens = coalesce(:allergens, p.allergens),
	  p.dietGoal = coalesce(:dietGoal, p.dietGoal),
	  p.dislikedIngredients = coalesce(:dislikedIngredients, p.dislikedIngredients)
	where p.userId = :userId
	""")
	int updatePersonalization(Long userId, String allergens, Double dietGoal, String dislikedIngredients);
}
