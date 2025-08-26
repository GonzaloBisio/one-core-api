package com.one.core.application.dto.tenant.purchases;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDate;
import java.util.List;

@Data
public class PurchaseOrderRequestDTO {
    private Long supplierId;
    private LocalDate expectedDeliveryDate;
    @NotEmpty private List<@Valid PurchaseOrderItemRequestDTO> items;
    private String notes;
}