package com.one.core.application.dto.tenant.product;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductFilterDTO {
    private String name;
    private String sku;
    private Long categoryId;
    private Boolean isActive;
    private String productType;
    // Puedes añadir más campos de filtro según necesites (ej. rango de precios)
}