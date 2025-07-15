package com.one.core.domain.repository.tenant.purchases;

import com.one.core.domain.model.enums.purchases.PurchaseOrderStatus;
import com.one.core.domain.model.tenant.purchases.PurchaseOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

@Repository
public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, Long>, JpaSpecificationExecutor<PurchaseOrder> {
    List<PurchaseOrder> findByOrderDateBetweenAndStatusNot(LocalDate startDate, LocalDate endDate, PurchaseOrderStatus status);
    List<PurchaseOrder> findByCreatedAtBetweenAndStatusNot(OffsetDateTime startDateTime, OffsetDateTime endDateTime, PurchaseOrderStatus status);
    List<PurchaseOrder> findByOrderDateAndStatusNot(LocalDate orderDate, PurchaseOrderStatus status);

    @Query("SELECT p FROM PurchaseOrder p WHERE p.createdAt BETWEEN :startDateTime AND :endDateTime AND p.status <> :status")
    List<PurchaseOrder> findByDateTimeRangeAndStatusNot(
            @Param("startDateTime") OffsetDateTime startDateTime,
            @Param("endDateTime") OffsetDateTime endDateTime,
            @Param("status") PurchaseOrderStatus status
    );

}
