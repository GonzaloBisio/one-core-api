package com.one.core.application.dto.tenant.expenses;


import com.one.core.domain.model.enums.expenses.FixedExpenseCategory;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class ExpenseLogRequestDTO {
    private Long fixedExpenseId;

    private String description;

    private BigDecimal amount;

    private FixedExpenseCategory category;

    @NotNull
    private LocalDate expenseDate;
}