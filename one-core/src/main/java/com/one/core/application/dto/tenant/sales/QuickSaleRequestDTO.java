package com.one.core.application.dto.tenant.sales;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import java.util.List;

@Data
public class QuickSaleRequestDTO {
    private Long customerId;

    @NotEmpty
    private List<QuickSaleItemRequestDTO> items;
}