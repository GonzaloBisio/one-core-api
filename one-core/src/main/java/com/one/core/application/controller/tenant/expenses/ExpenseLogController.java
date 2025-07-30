package com.one.core.application.controller.tenant.expenses;

import com.one.core.application.dto.tenant.expenses.ExpenseLogDTO;
import com.one.core.application.dto.tenant.expenses.ExpenseLogFilterDTO;
import com.one.core.application.dto.tenant.expenses.ExpenseLogRequestDTO;
import com.one.core.application.dto.tenant.response.PageableResponse;
import com.one.core.application.security.UserPrincipal;
import com.one.core.domain.service.tenant.expenses.ExpenseLogService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/expense-logs") // Cambiado a /expense-logs para diferenciarlo del otro controller
@PreAuthorize("hasAnyRole('TENANT_ADMIN', 'SUPER_ADMIN')")
public class ExpenseLogController {

    private final ExpenseLogService expenseLogService;

    @Autowired
    public ExpenseLogController(ExpenseLogService expenseLogService) {
        this.expenseLogService = expenseLogService;
    }

    @PostMapping
    public ResponseEntity<ExpenseLogDTO> createExpenseLog(@Valid @RequestBody ExpenseLogRequestDTO requestDTO, @AuthenticationPrincipal UserPrincipal currentUser) {
        ExpenseLogDTO createdExpense = expenseLogService.createExpenseLog(requestDTO, currentUser);
        return new ResponseEntity<>(createdExpense, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<PageableResponse<ExpenseLogDTO>> getAllExpenseLogs(
            ExpenseLogFilterDTO filterDTO,
            @PageableDefault(size = 20, sort = "expenseDate", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<ExpenseLogDTO> expensePage = expenseLogService.getAllExpenseLogs(filterDTO, pageable);
        return ResponseEntity.ok(new PageableResponse<>(expensePage));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteExpenseLog(@PathVariable Long id) {
        expenseLogService.deleteExpenseLog(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ExpenseLogDTO> getExpenseLogById(@PathVariable Long id) {
        return ResponseEntity.ok(expenseLogService.getExpenseLogById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ExpenseLogDTO> updateExpenseLog(@PathVariable Long id, @Valid @RequestBody ExpenseLogRequestDTO requestDTO) {
        return ResponseEntity.ok(expenseLogService.updateExpenseLog(id, requestDTO));
    }}