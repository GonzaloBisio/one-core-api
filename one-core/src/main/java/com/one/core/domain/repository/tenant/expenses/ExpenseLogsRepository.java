package com.one.core.domain.repository.tenant.expenses;

import com.one.core.domain.model.tenant.expenses.ExpenseLogs;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ExpenseLogsRepository extends JpaRepository<ExpenseLogs, Long>, JpaSpecificationExecutor<ExpenseLogs> {
    List<ExpenseLogs> findByExpenseDateBetween(LocalDate startDate, LocalDate endDate);
}