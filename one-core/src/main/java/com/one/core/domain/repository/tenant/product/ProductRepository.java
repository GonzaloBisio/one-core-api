package com.one.core.domain.repository.tenant.product;

import com.one.core.domain.model.enums.ProductType;
import com.one.core.domain.model.tenant.product.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {
    Optional<Product> findBySku(String sku);
    boolean existsBySku(String sku);
    boolean existsByName(String name);
    Optional<Product> findByCanonicalName(String canonicalName);
    boolean existsByCanonicalName(String canonicalName);
    List<Product> findAllByProductTypeAndIsActiveTrueOrderByNameAsc(ProductType productType);
    List<Product> findAllByProductTypeOrderByNameAsc(ProductType productType);

}