package com.ongi.api.product.adapter.out.persistence.repository;

import com.ongi.api.product.adapter.out.persistence.ProductEntity;
import jakarta.persistence.LockModeType;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ProductRepository extends JpaRepository<ProductEntity, Long> {

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select p from ProductEntity p where p.id in :ids")
	List<ProductEntity> findAllByIdsWithLock(@Param("ids") List<Long> ids);
}
