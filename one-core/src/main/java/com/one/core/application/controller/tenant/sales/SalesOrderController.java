// src/main/java/com/one/core/application/controller/tenant/sales/SalesOrderController.java
package com.one.core.application.controller.tenant.sales;

import com.one.core.application.dto.tenant.response.PageableResponse;
import com.one.core.application.dto.tenant.sales.SalesOrderDTO;
import com.one.core.application.dto.tenant.sales.SalesOrderFilterDTO;
import com.one.core.application.dto.tenant.sales.SalesOrderRequestDTO;
import com.one.core.domain.service.tenant.sales.SalesOrderService;
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
@RequestMapping("/sales-orders")
@PreAuthorize("hasAnyRole('TENANT_USER', 'TENANT_ADMIN', 'SALES_PERSON', 'SUPER_ADMIN')")
public class SalesOrderController {

    private final SalesOrderService salesOrderService;

    @Autowired
    public SalesOrderController(SalesOrderService salesOrderService) {
        this.salesOrderService = salesOrderService;
    }

    @PostMapping
    public ResponseEntity<SalesOrderDTO> createSalesOrder(@Valid @RequestBody SalesOrderRequestDTO requestDTO) {
        SalesOrderDTO createdOrder = salesOrderService.createSalesOrder(requestDTO);
        return new ResponseEntity<>(createdOrder, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<SalesOrderDTO> getSalesOrderById(@PathVariable Long id) {
        return ResponseEntity.ok(salesOrderService.getSalesOrderById(id));
    }

    @GetMapping
    public ResponseEntity<PageableResponse<SalesOrderDTO>> getAllSalesOrders(
            SalesOrderFilterDTO filterDTO,
            @PageableDefault(size = 10, sort = "orderDate", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<SalesOrderDTO> orderPage = salesOrderService.getAllSalesOrders(filterDTO, pageable);
        PageableResponse<SalesOrderDTO> response = new PageableResponse<>(orderPage);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/confirm-process")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'SALES_MANAGER', 'SUPER_ADMIN')") // Roles con más permisos
    public ResponseEntity<SalesOrderDTO> confirmAndProcessSalesOrder(@PathVariable Long id) {
        SalesOrderDTO processedOrder = salesOrderService.confirmAndProcessSalesOrder(id);
        return ResponseEntity.ok(processedOrder);
    }

    // Aquí podrías añadir más endpoints para actualizar estado (ej. a SHIPPED, DELIVERED, CANCELLED)
}