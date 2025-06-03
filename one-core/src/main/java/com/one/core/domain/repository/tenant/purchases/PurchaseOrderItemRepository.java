package com.one.core.domain.repository.tenant.purchases;

import com.one.core.domain.model.tenant.purchases.PurchaseOrderItem;
import com.one.core.domain.model.tenant.sales.SalesOrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PurchaseOrderItemRepository extends JpaRepository<PurchaseOrderItem, Long> {
}