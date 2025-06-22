package com.one.core.application.controller.tenant.inventory;

import com.one.core.application.dto.tenant.inventory.StockAdjustmentRequestDTO;
import com.one.core.application.dto.tenant.inventory.StockMovementDTO;
import com.one.core.application.dto.tenant.inventory.StockMovementFilterDTO;
import com.one.core.application.dto.tenant.response.PageableResponse; // Tu DTO de respuesta paginada
import com.one.core.domain.service.tenant.inventory.InventoryService;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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

    /**
     * Realiza un ajuste manual de stock.
     * Requiere un rol con más permisos, como un administrador de inventario o de tenant.
     */
    @PostMapping("/adjustments")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'INVENTORY_MANAGER', 'SUPER_ADMIN')") // Roles más específicos
    public ResponseEntity<StockMovementDTO> performManualAdjustment(
            @Valid @RequestBody StockAdjustmentRequestDTO adjustmentDTO) {
        StockMovementDTO movementDTO = inventoryService.performManualStockAdjustment(adjustmentDTO);
        return ResponseEntity.ok(movementDTO);
    }

    /**
     * Obtiene una lista paginada y filtrada de todos los movimientos de stock.
     */
    @GetMapping("/movements")
    @PreAuthorize("hasAnyRole('TENANT_USER', 'TENANT_ADMIN', 'INVENTORY_MANAGER', 'SUPER_ADMIN')") // Acceso más general
    public ResponseEntity<PageableResponse<StockMovementDTO>> getStockMovements(
            StockMovementFilterDTO filterDTO,
            @PageableDefault(size = 10, sort = "movementDate", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<StockMovementDTO> movementPage = inventoryService.getStockMovements(filterDTO, pageable);
        PageableResponse<StockMovementDTO> response = new PageableResponse<>(movementPage);
        return ResponseEntity.ok(response);
    }

    /**
     * Obtiene el nivel de stock actual para un producto específico.
     */
    @GetMapping("/products/{productId}/stock")
    @PreAuthorize("hasAnyRole('TENANT_USER', 'TENANT_ADMIN', 'INVENTORY_MANAGER', 'SUPER_ADMIN')")
    public ResponseEntity<BigDecimal> getCurrentStock(@PathVariable Long productId) {
        BigDecimal currentStock = inventoryService.getCurrentStock(productId);
        return ResponseEntity.ok(currentStock);
    }

    /**
     * Verifica si hay una cantidad específica de stock disponible para un producto.
     */
    @GetMapping("/products/{productId}/is-available")
    @PreAuthorize("hasAnyRole('TENANT_USER', 'TENANT_ADMIN', 'INVENTORY_MANAGER', 'SUPER_ADMIN')")
    public ResponseEntity<Boolean> isStockAvailable(
            @PathVariable Long productId,
            @RequestParam BigDecimal quantityNeeded) {
        boolean available = inventoryService.isStockAvailable(productId, quantityNeeded);
        return ResponseEntity.ok(available);
    }
}