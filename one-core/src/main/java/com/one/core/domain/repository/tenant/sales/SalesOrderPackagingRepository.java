package com.one.core.domain.repository.tenant.sales;

import com.one.core.domain.model.tenant.sales.SalesOrderPackaging;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SalesOrderPackagingRepository extends JpaRepository<SalesOrderPackaging, Long> {
    List<SalesOrderPackaging> findBySalesOrderId(Long salesOrderId);
}
