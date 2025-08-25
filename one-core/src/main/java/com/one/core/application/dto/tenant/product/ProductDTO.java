package com.one.core.application.dto.tenant.product;

import com.one.core.domain.model.enums.ProductType;
import com.one.core.domain.model.enums.UnitOfMeasure;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class ProductDTO {
    private Long id;
    private String sku;
    private String name;
    private String description;
    private ProductType productType;
    private boolean hasPackaging; // Indica si el producto tiene empaques
    private Long categoryId; // O un CategoryDTO anidado
    private String categoryName;
    private Long defaultSupplierId; // O un SupplierDTO anidado
    private String defaultSupplierName;
    private BigDecimal purchasePrice;
    private BigDecimal salePrice;
    private UnitOfMeasure unitOfMeasure;
    private BigDecimal currentStock;
    private BigDecimal minimumStockLevel;
    private boolean isActive;
    private String barcode;
    private String imageUrl;
}