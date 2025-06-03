package com.one.core.domain.repository.tenant.product;

import com.one.core.domain.model.tenant.product.StockMovement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface StockMovementRepository extends JpaRepository<StockMovement, Long>, JpaSpecificationExecutor<StockMovement> {
    Page<StockMovement> findByProductIdOrderByMovementDateDesc(Long productId, Pageable pageable);
}