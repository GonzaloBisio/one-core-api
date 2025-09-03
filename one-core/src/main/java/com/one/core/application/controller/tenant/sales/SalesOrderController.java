package com.one.core.application.controller.tenant.sales;

import com.one.core.application.dto.tenant.response.PageableResponse;
import com.one.core.application.dto.tenant.sales.SalesOrderDTO;
import com.one.core.application.dto.tenant.sales.SalesOrderFilterDTO;
import com.one.core.application.dto.tenant.sales.SalesOrderRequestDTO;
import com.one.core.application.security.UserPrincipal;
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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
    public ResponseEntity<SalesOrderDTO> createSalesOrder(
            @Valid @RequestBody SalesOrderRequestDTO requestDTO,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        SalesOrderDTO createdOrder = salesOrderService.createSalesOrder(requestDTO, currentUser);
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
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'SALES_MANAGER', 'SUPER_ADMIN')")
    public ResponseEntity<SalesOrderDTO> confirmAndProcessSalesOrder(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        SalesOrderDTO processedOrder = salesOrderService.confirmAndProcessSalesOrder(id, currentUser);
        return ResponseEntity.ok(processedOrder);
    }

    @PostMapping("/{id}/ship")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'WAREHOUSE_STAFF', 'SUPER_ADMIN')") // Roles que pueden despachar
    public ResponseEntity<SalesOrderDTO> shipOrder(@PathVariable Long id) {
        SalesOrderDTO shippedOrder = salesOrderService.shipOrder(id);
        return ResponseEntity.ok(shippedOrder);
    }

    @PostMapping("/{id}/deliver")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'WAREHOUSE_STAFF', 'SUPER_ADMIN')") // Roles que pueden marcar como entregado
    public ResponseEntity<SalesOrderDTO> deliverOrder(@PathVariable Long id) {
        SalesOrderDTO deliveredOrder = salesOrderService.deliverOrder(id);
        return ResponseEntity.ok(deliveredOrder);
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'SALES_MANAGER', 'SUPER_ADMIN')") // Roles que pueden cancelar
    public ResponseEntity<SalesOrderDTO> cancelOrder(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        SalesOrderDTO cancelledOrder = salesOrderService.cancelOrder(id, currentUser);
        return ResponseEntity.ok(cancelledOrder);
    }

}