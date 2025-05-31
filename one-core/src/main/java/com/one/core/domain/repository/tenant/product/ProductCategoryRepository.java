package com.one.core.domain.repository.tenant.product;

import com.one.core.domain.model.tenant.product.Product;
import com.one.core.domain.model.tenant.product.ProductCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductCategoryRepository extends JpaRepository<ProductCategory, Long>, JpaSpecificationExecutor<ProductCategory> {
    Optional<ProductCategory> findByName(String name);
    boolean existsByName(String name);
    Optional<ProductCategory> findByDescription(String description);
}