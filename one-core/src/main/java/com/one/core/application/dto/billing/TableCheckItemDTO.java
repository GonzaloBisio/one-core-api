package com.one.core.application.dto.billing;

import java.math.BigDecimal;
import lombok.Data;

/**
 * Representation of an item within a table check.
 */
@Data
public class TableCheckItemDTO {
    private Long id;
    private Long productId;
    private String productName;
    private BigDecimal quantity;
    private BigDecimal unitPriceAtSale;
    private BigDecimal discountPerItem;
    private BigDecimal subtotal;
}

