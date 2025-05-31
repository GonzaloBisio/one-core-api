package com.one.core.application.dto.tenant.product;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductCategoryFilterDTO {
    private String name;
    private String description;
    private Boolean hasParent; // Filtrar si tiene o no categoría padre

    // Puedes añadir más campos de filtro según necesites (ej. rango de precios)
}