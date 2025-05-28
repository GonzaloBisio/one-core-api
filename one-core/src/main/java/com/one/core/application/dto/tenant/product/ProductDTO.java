package com.one.core.application.dto.tenant.product;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class ProductDTO {
    private Long id;
    private String sku;
    private String name;
    private String description;
    private Long categoryId; // O un CategoryDTO anidado
    private String categoryName;
    private Long defaultSupplierId; // O un SupplierDTO anidado
    private String defaultSupplierName;
    private BigDecimal purchasePrice;
    private BigDecimal salePrice;
    private String unitOfMeasure;
    private BigDecimal currentStock;
    private BigDecimal minimumStockLevel;
    private boolean isActive;
    private String barcode;
    private String imageUrl;
}