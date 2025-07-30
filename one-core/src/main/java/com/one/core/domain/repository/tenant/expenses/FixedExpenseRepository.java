package com.one.core.domain.repository.tenant.expenses;

import com.one.core.domain.model.tenant.expenses.FixedExpense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FixedExpenseRepository extends JpaRepository<FixedExpense, Long> {
    // Por ahora no necesita métodos custom. Se pueden añadir después si se necesitan.
}