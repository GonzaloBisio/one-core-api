package com.one.core.application.dto.tenant.expenses;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class FixedExpenseRequestDTO {
    @NotBlank
    private String name;
    @NotBlank
    private String category;
    @NotNull @Positive
    private BigDecimal currentAmount;
    private String notes;
}