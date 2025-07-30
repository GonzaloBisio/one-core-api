package com.one.core.application.mapper.expenses;

import com.one.core.application.dto.tenant.expenses.FixedExpenseDTO;
import com.one.core.application.dto.tenant.expenses.FixedExpenseRequestDTO;
import com.one.core.domain.model.tenant.expenses.FixedExpense;
import org.springframework.stereotype.Component;

@Component
public class FixedExpenseMapper {

    public FixedExpenseDTO toDTO(FixedExpense entity) {
        if (entity == null) return null;
        FixedExpenseDTO dto = new FixedExpenseDTO();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setCategory(entity.getCategory());
        dto.setCurrentAmount(entity.getCurrentAmount());
        dto.setNotes(entity.getNotes());
        dto.setActive(entity.isActive());
        return dto;
    }

    public FixedExpense toEntity(FixedExpenseRequestDTO dto) {
        if (dto == null) return null;
        FixedExpense entity = new FixedExpense();
        updateEntityFromDTO(dto, entity);
        return entity;
    }

    public void updateEntityFromDTO(FixedExpenseRequestDTO dto, FixedExpense entity) {
        entity.setName(dto.getName());
        entity.setCategory(dto.getCategory());
        entity.setCurrentAmount(dto.getCurrentAmount());
        entity.setNotes(dto.getNotes());
    }
}