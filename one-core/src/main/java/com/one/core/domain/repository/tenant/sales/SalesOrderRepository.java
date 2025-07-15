package com.one.core.domain.repository.tenant.sales;

import com.one.core.domain.model.enums.sales.SalesOrderStatus;
import com.one.core.domain.model.tenant.sales.SalesOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

@Repository
public interface SalesOrderRepository extends JpaRepository<SalesOrder, Long>, JpaSpecificationExecutor<SalesOrder> {

    List<SalesOrder> findByOrderDateBetweenAndStatusNot(LocalDate startDate, LocalDate endDate, SalesOrderStatus status);
    List<SalesOrder> findByCreatedAtBetweenAndStatusNot(OffsetDateTime startDateTime, OffsetDateTime endDateTime, SalesOrderStatus status);
    List<SalesOrder> findByOrderDateAndStatusNot(LocalDate orderDate, SalesOrderStatus status);

    @Query("SELECT s FROM SalesOrder s WHERE s.createdAt BETWEEN :startDateTime AND :endDateTime AND s.status <> :status")
    List<SalesOrder> findByDateTimeRangeAndStatusNot(
            @Param("startDateTime") OffsetDateTime startDateTime,
            @Param("endDateTime") OffsetDateTime endDateTime,
            @Param("status") SalesOrderStatus status
    );
}