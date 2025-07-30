package com.one.core.domain.service.tenant.expenses;

import com.one.core.application.dto.tenant.expenses.ExpenseLogDTO;
import com.one.core.application.dto.tenant.expenses.ExpenseLogFilterDTO;
import com.one.core.application.dto.tenant.expenses.ExpenseLogRequestDTO;
import com.one.core.application.exception.ResourceNotFoundException;
import com.one.core.application.exception.ValidationException;
import com.one.core.application.mapper.expenses.ExpenseLogMapper;
import com.one.core.application.security.UserPrincipal;
import com.one.core.domain.model.tenant.expenses.ExpenseLogs;
import com.one.core.domain.model.tenant.expenses.FixedExpense;
import com.one.core.domain.repository.admin.SystemUserRepository;
import com.one.core.domain.repository.tenant.expenses.ExpenseLogsRepository;
import com.one.core.domain.repository.tenant.expenses.FixedExpenseRepository;
import com.one.core.domain.service.tenant.expenses.criteria.ExpenseLogSpecification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ExpenseLogService {

    private final ExpenseLogsRepository expenseLogsRepository;
    private final FixedExpenseRepository fixedExpenseRepository;
    private final ExpenseLogMapper expenseLogMapper;
    private final SystemUserRepository systemUserRepository;

    @Autowired
    public ExpenseLogService(ExpenseLogsRepository expenseLogsRepository, FixedExpenseRepository fixedExpenseRepository, ExpenseLogMapper expenseLogMapper, SystemUserRepository systemUserRepository) {
        this.expenseLogsRepository = expenseLogsRepository;
        this.fixedExpenseRepository = fixedExpenseRepository;
        this.expenseLogMapper = expenseLogMapper;
        this.systemUserRepository = systemUserRepository;
    }

    @Transactional
    public ExpenseLogDTO createExpenseLog(ExpenseLogRequestDTO requestDTO, UserPrincipal currentUser) {
        ExpenseLogs expenseLog = new ExpenseLogs();

        if (requestDTO.getFixedExpenseId() != null) {
            FixedExpense template = fixedExpenseRepository.findById(requestDTO.getFixedExpenseId())
                    .orElseThrow(() -> new ResourceNotFoundException("FixedExpense", "id", requestDTO.getFixedExpenseId()));
            expenseLog.setDescription(template.getName());
            expenseLog.setAmount(template.getCurrentAmount());
            expenseLog.setCategory(template.getCategory());
            expenseLog.setFixedExpense(template);
        } else {
            if (requestDTO.getDescription() == null || requestDTO.getAmount() == null || requestDTO.getCategory() == null) {
                throw new ValidationException("Description, amount, and category are required for variable expenses.");
            }
            expenseLog.setDescription(requestDTO.getDescription());
            expenseLog.setAmount(requestDTO.getAmount());
            expenseLog.setCategory(String.valueOf(requestDTO.getCategory()));
        }

        expenseLog.setExpenseDate(requestDTO.getExpenseDate());
        expenseLog.setCreatedByUser(systemUserRepository.getReferenceById(currentUser.getId()));

        ExpenseLogs savedLog = expenseLogsRepository.save(expenseLog);
        return expenseLogMapper.toDTO(savedLog);
    }

    @Transactional(readOnly = true)
    public Page<ExpenseLogDTO> getAllExpenseLogs(ExpenseLogFilterDTO filterDTO, Pageable pageable) {
        return expenseLogsRepository.findAll(ExpenseLogSpecification.filterBy(filterDTO), pageable)
                .map(expenseLogMapper::toDTO);
    }

    @Transactional
    public void deleteExpenseLog(Long id) {
        if (!expenseLogsRepository.existsById(id)) {
            throw new ResourceNotFoundException("ExpenseLog", "id", id);
        }
        expenseLogsRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public ExpenseLogDTO getExpenseLogById(Long id) {
        return expenseLogsRepository.findById(id)
                .map(expenseLogMapper::toDTO)
                .orElseThrow(() -> new ResourceNotFoundException("ExpenseLog", "id", id));
    }

    @Transactional
    public ExpenseLogDTO updateExpenseLog(Long id, ExpenseLogRequestDTO requestDTO) {
        ExpenseLogs expenseLog = expenseLogsRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ExpenseLog", "id", id));

        if (expenseLog.getFixedExpense() != null && (requestDTO.getDescription() != null || requestDTO.getAmount() != null || requestDTO.getCategory() != null)) {
            throw new ValidationException("Cannot modify details of an expense log linked to a fixed expense template.");
        }

        expenseLogMapper.updateEntityFromDTO(requestDTO, expenseLog);

        ExpenseLogs updatedLog = expenseLogsRepository.save(expenseLog);
        return expenseLogMapper.toDTO(updatedLog);
    }

}