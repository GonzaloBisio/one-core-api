package com.one.core.application.dto.tenant.purchases;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDate;
import java.util.List;

@Data
public class GoodsReceiptRequestDTO {
    @NotNull private Long purchaseOrderId;
    private LocalDate receiptDate; // Fecha de recepción, por defecto hoy si es null
    @NotEmpty private List<@Valid GoodsReceiptItemDTO> itemsReceived;
    private String notes;
}