// src/main/java/com/one/core/application/controller/tenant/purchases/PurchaseOrderController.java
package com.one.core.application.controller.tenant.purchases;

import com.one.core.application.dto.tenant.purchases.GoodsReceiptRequestDTO;
import com.one.core.application.dto.tenant.purchases.PurchaseOrderDTO;
import com.one.core.application.dto.tenant.purchases.PurchaseOrderFilterDTO;
import com.one.core.application.dto.tenant.purchases.PurchaseOrderRequestDTO;
import com.one.core.application.dto.tenant.response.PageableResponse;
import com.one.core.domain.service.tenant.purchases.PurchaseOrderService;
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
@RequestMapping("/purchase-orders")
@PreAuthorize("hasAnyRole('TENANT_USER', 'TENANT_ADMIN', 'PURCHASING_MANAGER', 'SUPER_ADMIN')")
public class PurchaseOrderController {

    private final PurchaseOrderService purchaseOrderService;

    @Autowired
    public PurchaseOrderController(PurchaseOrderService purchaseOrderService) {
        this.purchaseOrderService = purchaseOrderService;
    }

    @PostMapping
    public ResponseEntity<PurchaseOrderDTO> createPurchaseOrder(@Valid @RequestBody PurchaseOrderRequestDTO requestDTO) {
        PurchaseOrderDTO createdOrder = purchaseOrderService.createPurchaseOrder(requestDTO);
        return new ResponseEntity<>(createdOrder, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PurchaseOrderDTO> getPurchaseOrderById(@PathVariable Long id) {
        return ResponseEntity.ok(purchaseOrderService.getPurchaseOrderById(id));
    }

    @GetMapping
    public ResponseEntity<PageableResponse<PurchaseOrderDTO>> getAllPurchaseOrders(
            PurchaseOrderFilterDTO filterDTO,
            @PageableDefault(size = 20, sort = "orderDate", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<PurchaseOrderDTO> orderPage = purchaseOrderService.getAllPurchaseOrders(filterDTO, pageable);
        PageableResponse<PurchaseOrderDTO> response = new PageableResponse<>(orderPage);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/receive-goods")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'INVENTORY_MANAGER', 'SUPER_ADMIN')") // Roles con permisos para registrar recepción
    public ResponseEntity<PurchaseOrderDTO> receiveGoods(@Valid @RequestBody GoodsReceiptRequestDTO receiptDTO) {
        PurchaseOrderDTO updatedOrder = purchaseOrderService.receiveGoods(receiptDTO);
        return ResponseEntity.ok(updatedOrder);
    }

    // Endpoints para actualizar estado (ej. DRAFT -> ORDERED), cancelar orden, etc.
}