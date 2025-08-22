package com.one.core.application.controller;

import com.one.core.application.dto.billing.PaymentRequestDTO;
import com.one.core.application.dto.billing.TableCheckDTO;
import com.one.core.application.dto.billing.TableCheckItemRequestDTO;
import com.one.core.application.mapper.billing.TableBillingMapper;
import com.one.core.domain.model.tenant.sales.SalesOrder;
import com.one.core.domain.model.tenant.sales.SalesOrderItem;
import com.one.core.domain.service.billing.TableBillingService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller exposing endpoints to manage table checks.
 */
@RestController
@RequestMapping("/tables")
@PreAuthorize("hasAnyRole('TENANT_USER','TENANT_ADMIN','SUPER_ADMIN')")
public class TableBillingController {

    private final TableBillingService tableBillingService;
    private final TableBillingMapper tableBillingMapper;

    @Autowired
    public TableBillingController(TableBillingService tableBillingService,
                                  TableBillingMapper tableBillingMapper) {
        this.tableBillingService = tableBillingService;
        this.tableBillingMapper = tableBillingMapper;
    }

    /**
     * Opens a new check for the given table.
     */
    @PostMapping("/{tableId}/checks")
    public ResponseEntity<TableCheckDTO> openCheck(@PathVariable Long tableId) {
        tableBillingService.openCheck(tableId);
        SalesOrder order = tableBillingService.closeCheck(tableId);
        return new ResponseEntity<>(tableBillingMapper.toDTO(order), HttpStatus.CREATED);
    }

    /**
     * Adds an item to the table's open check.
     */
    @PostMapping("/{tableId}/checks/{checkId}/items")
    public ResponseEntity<TableCheckDTO> addItem(@PathVariable Long tableId,
                                                 @PathVariable Long checkId,
                                                 @Valid @RequestBody TableCheckItemRequestDTO itemDto) {
        SalesOrderItem item = tableBillingMapper.toEntity(itemDto);
        tableBillingService.addItem(tableId, item);
        SalesOrder order = tableBillingService.closeCheck(tableId);
        return ResponseEntity.ok(tableBillingMapper.toDTO(order));
    }

    /**
     * Registers a payment for the open check and closes it.
     */
    @PostMapping("/{tableId}/checks/{checkId}/payments")
    public ResponseEntity<Void> payCheck(@PathVariable Long tableId,
                                         @PathVariable Long checkId,
                                         @Valid @RequestBody PaymentRequestDTO paymentDto) {
        tableBillingService.registerPayment(tableId, paymentDto.getAmount());
        return ResponseEntity.ok().build();
    }

    /**
     * Retrieves the details of the current open check for the table.
     */
    @GetMapping("/{tableId}/checks/open")
    public ResponseEntity<TableCheckDTO> getOpenCheck(@PathVariable Long tableId) {
        SalesOrder order = tableBillingService.closeCheck(tableId);
        return ResponseEntity.ok(tableBillingMapper.toDTO(order));
    }
}

