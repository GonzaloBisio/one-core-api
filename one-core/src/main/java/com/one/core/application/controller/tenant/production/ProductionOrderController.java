package com.one.core.application.controller.tenant.production;

import com.one.core.application.dto.tenant.production.ProductionOrderDTO;
import com.one.core.application.dto.tenant.production.ProductionOrderRequestDTO;
import com.one.core.application.security.UserPrincipal;
import com.one.core.domain.service.tenant.production.ProductionOrderService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/production-orders")
@PreAuthorize("hasAnyRole('TENANT_ADMIN', 'PRODUCTION_MANAGER', 'SUPER_ADMIN')")
public class ProductionOrderController {

    private final ProductionOrderService productionOrderService;

    @Autowired
    public ProductionOrderController(ProductionOrderService productionOrderService) {
        this.productionOrderService = productionOrderService;
    }

    @PostMapping
    public ResponseEntity<ProductionOrderDTO> createProductionOrder(
            @Valid @RequestBody ProductionOrderRequestDTO requestDTO,
            @AuthenticationPrincipal UserPrincipal currentUser) {

        ProductionOrderDTO createdOrder = productionOrderService.createProductionOrder(requestDTO, currentUser);
        return new ResponseEntity<>(createdOrder, HttpStatus.CREATED);
    }


    @GetMapping
    public ResponseEntity<List<ProductionOrderDTO>> getAllProductionOrders() {
        List<ProductionOrderDTO> productionOrders = productionOrderService.getAllProductionOrders();
        return new ResponseEntity<>(productionOrders, HttpStatus.OK);
    }
    // Aquí podrías añadir endpoints GET para listar/ver órdenes de producción
}