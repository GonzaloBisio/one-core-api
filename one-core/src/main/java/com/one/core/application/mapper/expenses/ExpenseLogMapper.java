package com.one.core.application.mapper.expenses;

import com.one.core.application.dto.tenant.expenses.ExpenseLogDTO;
import com.one.core.application.dto.tenant.expenses.ExpenseLogRequestDTO;
import com.one.core.domain.model.enums.expenses.FixedExpenseCategory;
import com.one.core.domain.model.tenant.expenses.ExpenseLogs;
import org.springframework.stereotype.Component;

@Component
public class ExpenseLogMapper {

    public ExpenseLogDTO toDTO(ExpenseLogs entity) {
        if (entity == null) return null;

        ExpenseLogDTO dto = new ExpenseLogDTO();
        dto.setId(entity.getId());
        dto.setDescription(entity.getDescription());
        dto.setAmount(entity.getAmount());
        dto.setExpenseDate(entity.getExpenseDate());
        dto.setCategory(FixedExpenseCategory.valueOf(entity.getCategory()));
        dto.setCreatedAt(entity.getCreatedAt());

        if (entity.getFixedExpense() != null) {
            dto.setFixedExpenseId(entity.getFixedExpense().getId());
            dto.setFixedExpenseName(entity.getFixedExpense().getName());
        }

        if (entity.getCreatedByUser() != null) {
            dto.setCreatedByUserId(entity.getCreatedByUser().getId());
            dto.setCreatedByUsername(entity.getCreatedByUser().getUsername());
        }

        return dto;
    }

    public void updateEntityFromDTO(ExpenseLogRequestDTO dto, ExpenseLogs entity) {
        // Solo se actualizan los campos si el gasto es variable (no ligado a una plantilla)
        if (entity.getFixedExpense() == null) {
            entity.setDescription(dto.getDescription());
            entity.setAmount(dto.getAmount());
            entity.setCategory(String.valueOf(dto.getCategory()));
        }
        entity.setExpenseDate(dto.getExpenseDate());
    }
}