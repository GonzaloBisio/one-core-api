package com.one.core.application.dto.tenant.expenses;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class FixedExpenseDTO {
    private Long id;
    private String name;
    private String category;
    private BigDecimal currentAmount;
    private String notes;
    private boolean isActive;
}