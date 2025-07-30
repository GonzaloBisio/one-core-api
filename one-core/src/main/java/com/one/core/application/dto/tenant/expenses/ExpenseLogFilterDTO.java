package com.one.core.application.dto.tenant.expenses;


import lombok.Data;
import java.time.LocalDate;

@Data
public class ExpenseLogFilterDTO {
    private String category;
    private LocalDate dateFrom;
    private LocalDate dateTo;
}