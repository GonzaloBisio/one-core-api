package com.one.core.application.dto.tenant.expenses;


import com.one.core.domain.model.enums.expenses.FixedExpenseCategory;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Data
public class ExpenseLogDTO {
    private Long id;
    private String description;
    private BigDecimal amount;
    private LocalDate expenseDate;
    private FixedExpenseCategory category;
    private Long fixedExpenseId; // Para saber si está ligado a una plantilla
    private String fixedExpenseName;
    private Long createdByUserId;
    private String createdByUsername;
    private OffsetDateTime createdAt;
}