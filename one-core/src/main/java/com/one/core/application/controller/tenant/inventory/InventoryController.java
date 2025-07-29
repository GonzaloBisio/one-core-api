package com.one.core.application.controller.tenant.inventory;

import com.one.core.application.dto.tenant.inventory.StockAdjustmentRequestDTO;
import com.one.core.application.dto.tenant.inventory.StockMovementDTO;
import com.one.core.application.dto.tenant.inventory.StockMovementFilterDTO;
import com.one.core.application.dto.tenant.product.StockTransferRequestDTO;
import com.one.core.application.dto.tenant.response.PageableResponse;
import com.one.core.application.security.UserPrincipal;
import com.one.core.domain.service.tenant.inventory.InventoryService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/inventory")
@PreAuthorize("hasRole('TENANT_USER') or hasRole('SUPER_ADMIN') or hasRole('TENANT_ADMIN')")
public class InventoryController {

    private final InventoryService inventoryService;

    @Autowired
    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }

    @PostMapping("/adjustments")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'INVENTORY_MANAGER', 'SUPER_ADMIN')")
    public ResponseEntity<StockMovementDTO> performManualAdjustment(
            @Valid @RequestBody StockAdjustmentRequestDTO adjustmentDTO,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        StockMovementDTO movementDTO = inventoryService.performManualStockAdjustment(adjustmentDTO, currentUser);
        return ResponseEntity.ok(movementDTO);
    }

    @GetMapping("/movements")
    @PreAuthorize("hasAnyRole('TENANT_USER', 'TENANT_ADMIN', 'INVENTORY_MANAGER', 'SUPER_ADMIN')")
    public ResponseEntity<PageableResponse<StockMovementDTO>> getStockMovements(
            StockMovementFilterDTO filterDTO,
            @PageableDefault(size = 10, sort = "movementDate", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<StockMovementDTO> movementPage = inventoryService.getStockMovements(filterDTO, pageable);
        PageableResponse<StockMovementDTO> response = new PageableResponse<>(movementPage);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/products/{productId}/stock")
    @PreAuthorize("hasAnyRole('TENANT_USER', 'TENANT_ADMIN', 'INVENTORY_MANAGER', 'SUPER_ADMIN')")
    public ResponseEntity<BigDecimal> getCurrentStock(@PathVariable Long productId) {
        BigDecimal currentStock = inventoryService.getCurrentStock(productId);
        return ResponseEntity.ok(currentStock);
    }

    @GetMapping("/products/{productId}/is-available")
    @PreAuthorize("hasAnyRole('TENANT_USER', 'TENANT_ADMIN', 'INVENTORY_MANAGER', 'SUPER_ADMIN')")
    public ResponseEntity<Boolean> isStockAvailable(
            @PathVariable Long productId,
            @RequestParam BigDecimal quantityNeeded) {
        boolean available = inventoryService.isStockAvailable(productId, quantityNeeded);
        return ResponseEntity.ok(available);
    }

    @PostMapping("/stock/freeze")
    public ResponseEntity<Void> freezeStock(
            @Valid @RequestBody StockTransferRequestDTO request,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        inventoryService.transferToFrozenStock(request.getProductId(), request.getQuantity(), currentUser);

        return ResponseEntity.ok().build();
    }

    @PostMapping("/stock/thaw")
    public ResponseEntity<Void> thawStock(
            @Valid @RequestBody StockTransferRequestDTO request,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        inventoryService.thawStock(request.getProductId(), request.getQuantity(), currentUser);

        return ResponseEntity.ok().build();
    }
}