package com.one.core.application.controller.tenant.expenses;

import com.one.core.application.dto.tenant.expenses.FixedExpenseDTO;
import com.one.core.application.dto.tenant.expenses.FixedExpenseRequestDTO;
import com.one.core.domain.model.enums.expenses.FixedExpenseCategory;
import com.one.core.domain.service.tenant.expenses.FixedExpenseService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/fixed-expenses")
@PreAuthorize("hasAnyRole('TENANT_ADMIN', 'SUPER_ADMIN')")
public class FixedExpenseController {

    private final FixedExpenseService fixedExpenseService;

    @Autowired
    public FixedExpenseController(FixedExpenseService fixedExpenseService) {
        this.fixedExpenseService = fixedExpenseService;
    }

    @PostMapping
    public ResponseEntity<FixedExpenseDTO> createFixedExpense(@Valid @RequestBody FixedExpenseRequestDTO requestDTO) {
        FixedExpenseDTO created = fixedExpenseService.createFixedExpense(requestDTO);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<FixedExpenseDTO>> getAllFixedExpenses() {
        return ResponseEntity.ok(fixedExpenseService.getAllFixedExpenses());
    }

    @PutMapping("/{id}")
    public ResponseEntity<FixedExpenseDTO> updateFixedExpense(@PathVariable Long id, @Valid @RequestBody FixedExpenseRequestDTO requestDTO) {
        FixedExpenseDTO updated = fixedExpenseService.updateFixedExpense(id, requestDTO);
        return ResponseEntity.ok(updated);
    }

    @GetMapping("/categories")
    public ResponseEntity<List<Map<String, String>>> getAllCategories() {
        List<Map<String, String>> categories = Arrays.stream(FixedExpenseCategory.values())
                .map(category -> Map.of(
                        "value", category.name(),
                        "displayName", category.getDisplayName()
                ))
                .collect(Collectors.toList());
        return ResponseEntity.ok(categories);
    }

    // Aquí irían los endpoints de GET por ID y DELETE (soft delete)
}