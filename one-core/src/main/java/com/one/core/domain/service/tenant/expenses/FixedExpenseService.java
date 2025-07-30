package com.one.core.domain.service.tenant.expenses;

import com.one.core.application.dto.tenant.expenses.FixedExpenseDTO;
import com.one.core.application.dto.tenant.expenses.FixedExpenseRequestDTO;
import com.one.core.application.exception.ResourceNotFoundException;
import com.one.core.application.mapper.expenses.FixedExpenseMapper;
import com.one.core.domain.model.tenant.expenses.FixedExpense;
import com.one.core.domain.repository.tenant.expenses.FixedExpenseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FixedExpenseService {

    private final FixedExpenseRepository fixedExpenseRepository;
    private final FixedExpenseMapper fixedExpenseMapper; // <-- Inyectar el Mapper

    @Autowired
    public FixedExpenseService(FixedExpenseRepository fixedExpenseRepository, FixedExpenseMapper fixedExpenseMapper) {
        this.fixedExpenseRepository = fixedExpenseRepository;
        this.fixedExpenseMapper = fixedExpenseMapper;
    }

    @Transactional
    public FixedExpenseDTO createFixedExpense(FixedExpenseRequestDTO requestDTO) {
        FixedExpense fixedExpense = fixedExpenseMapper.toEntity(requestDTO);
        FixedExpense saved = fixedExpenseRepository.save(fixedExpense);
        return fixedExpenseMapper.toDTO(saved);
    }

    @Transactional
    public FixedExpenseDTO updateFixedExpense(Long id, FixedExpenseRequestDTO requestDTO) {
        FixedExpense fixedExpense = fixedExpenseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("FixedExpense", "id", id));
        fixedExpenseMapper.updateEntityFromDTO(requestDTO, fixedExpense);
        FixedExpense updated = fixedExpenseRepository.save(fixedExpense);
        return fixedExpenseMapper.toDTO(updated);
    }

    @Transactional(readOnly = true)
    public List<FixedExpenseDTO> getAllFixedExpenses() {
        return fixedExpenseRepository.findAll().stream()
                .map(fixedExpenseMapper::toDTO)
                .collect(Collectors.toList());
    }

    // Aquí puedes añadir métodos para getById y delete (soft delete: setActive(false))
}