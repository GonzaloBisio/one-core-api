package com.one.core.domain.repository.tenant.purchases;

import com.one.core.domain.model.enums.purchases.PurchaseOrderStatus;
import com.one.core.domain.model.tenant.purchases.PurchaseOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, Long>, JpaSpecificationExecutor<PurchaseOrder> {
    List<PurchaseOrder> findByOrderDateBetweenAndStatusNot(LocalDate startDate, LocalDate endDate, PurchaseOrderStatus status);

}
