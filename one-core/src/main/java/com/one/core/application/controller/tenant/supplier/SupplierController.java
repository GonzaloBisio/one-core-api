package com.one.core.application.controller.tenant.supplier;

import com.one.core.application.dto.tenant.response.PageableResponse;
import com.one.core.application.dto.tenant.supplier.SupplierDTO;
import com.one.core.application.dto.tenant.supplier.SupplierFilterDTO;
import com.one.core.domain.service.tenant.supplier.SupplierService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/suppliers")
@PreAuthorize("hasAnyRole('TENANT_ADMIN','SUPER_ADMIN')")
public class SupplierController {

    private final SupplierService supplierService;

    @Autowired
    public SupplierController(SupplierService supplierService) {
        this.supplierService = supplierService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('TENANT_ADMIN','SUPER_ADMIN')")
    public ResponseEntity<PageableResponse<SupplierDTO>> getAllSuppliers(
            SupplierFilterDTO filterDTO,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC)
            Pageable pageable
    ) {
        Page<SupplierDTO> supplierPage = supplierService.getAllSuppliers(filterDTO, pageable);
        return ResponseEntity.ok(new PageableResponse<>(supplierPage));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SupplierDTO> getSupplierById(@PathVariable Long id) {
        return ResponseEntity.ok(supplierService.getSupplierById(id));
    }

    @PostMapping
    public ResponseEntity<SupplierDTO> createSupplier(@Valid @RequestBody SupplierDTO supplierDTO) {
        SupplierDTO createdSupplier = supplierService.createSupplier(supplierDTO);
        return new ResponseEntity<>(createdSupplier, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<SupplierDTO> updateSupplier(@PathVariable Long id, @Valid @RequestBody SupplierDTO supplierDTO) {
        return ResponseEntity.ok(supplierService.updateSupplier(id, supplierDTO));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSupplier(@PathVariable Long id) {
        supplierService.deleteSupplier(id);
        return ResponseEntity.noContent().build();
    }
}