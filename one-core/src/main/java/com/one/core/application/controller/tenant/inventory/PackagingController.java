package com.one.core.application.controller.tenant.inventory;

import com.one.core.application.dto.tenant.product.PackagingConsumptionRequestDTO;
import com.one.core.application.dto.tenant.inventory.StockMovementDTO;
import com.one.core.application.security.UserPrincipal;
import com.one.core.domain.service.tenant.inventory.PackagingService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * REST endpoints for manual packaging stock management.
 */
@RestController
@RequestMapping("/packaging")
@PreAuthorize("hasRole('TENANT_USER') or hasRole('SUPER_ADMIN') or hasRole('TENANT_ADMIN')")
public class PackagingController {

    private final PackagingService packagingService;

    @Autowired
    public PackagingController(PackagingService packagingService) {
        this.packagingService = packagingService;
    }

    @PostMapping("/consume")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN', 'INVENTORY_MANAGER', 'SUPER_ADMIN')")
    public ResponseEntity<StockMovementDTO> consumePackaging(
            @Valid @RequestBody PackagingConsumptionRequestDTO request,
            @AuthenticationPrincipal UserPrincipal currentUser) {
        StockMovementDTO movement = packagingService.consumePackaging(request, currentUser);
        return ResponseEntity.ok(movement);
    }
}
