package com.one.core.domain.model.tenant.table;

import com.one.core.domain.model.tenant.product.Product;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.annotations.ColumnDefault;

import java.math.BigDecimal;

@Entity
@Table(name = "table_check_items")
@Data
@EqualsAndHashCode(exclude = {"tableCheck", "product"})
@ToString(exclude = {"tableCheck", "product"})
public class TableCheckItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "table_check_id", nullable = false)
    private TableCheck tableCheck;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false, precision = 10, scale = 3)
    private BigDecimal quantity;

    @Column(name = "unit_price_at_sale", nullable = false, precision = 12, scale = 2)
    private BigDecimal unitPriceAtSale;

    @Column(name = "discount_per_item", precision = 12, scale = 2)
    @ColumnDefault("0.00")
    private BigDecimal discountPerItem = BigDecimal.ZERO;

    @Transient
    private BigDecimal subtotal;

    public BigDecimal getSubtotal() {
        if (quantity != null && unitPriceAtSale != null) {
            BigDecimal discount = (discountPerItem != null) ? discountPerItem : BigDecimal.ZERO;
            return quantity.multiply(unitPriceAtSale.subtract(discount));
        }
        return BigDecimal.ZERO;
    }
}
