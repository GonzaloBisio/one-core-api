package com.one.core.domain.repository.tenant.sales;

import com.one.core.domain.model.enums.sales.SalesOrderStatus;
import com.one.core.domain.model.tenant.sales.SalesOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface SalesOrderRepository extends JpaRepository<SalesOrder, Long>, JpaSpecificationExecutor<SalesOrder> {

    List<SalesOrder> findByOrderDateBetweenAndStatusNot(LocalDate startDate, LocalDate endDate, SalesOrderStatus status);

}