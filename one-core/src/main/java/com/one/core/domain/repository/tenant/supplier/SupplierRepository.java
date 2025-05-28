package com.one.core.domain.repository.tenant.supplier;

import com.one.core.domain.model.tenant.supplier.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SupplierRepository extends JpaRepository<Supplier, Long> {
}