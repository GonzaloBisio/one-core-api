package com.one.core.domain.repository.tenant.product;

import com.one.core.domain.model.tenant.product.ProductPackaging;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ProductPackagingRepository extends JpaRepository<ProductPackaging, Long> {
    List<ProductPackaging> findByMainProductId(Long mainProductId);
}