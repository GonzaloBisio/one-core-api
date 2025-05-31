package com.one.core.domain.repository.tenant.supplier;

import com.one.core.domain.model.tenant.supplier.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SupplierRepository extends JpaRepository<Supplier, Long>, JpaSpecificationExecutor<Supplier> {
    Optional<Supplier> findByTaxId(String taxId);
    boolean existsByTaxId(String taxId);
    Optional<Supplier> findByEmail(String email); // Si el email debe ser único
    boolean existsByEmail(String email);
}