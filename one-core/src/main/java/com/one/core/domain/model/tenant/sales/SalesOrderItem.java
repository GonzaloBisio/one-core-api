package com.one.core.domain.model.tenant.sales;

import com.one.core.domain.model.tenant.product.Product;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.annotations.ColumnDefault;

import java.math.BigDecimal;

@Entity
@Table(name = "sales_order_items")
@Data
@EqualsAndHashCode(exclude = {"salesOrder", "product"})
@ToString(exclude = {"salesOrder", "product"})
public class SalesOrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "sales_order_id", nullable = false)
    private SalesOrder salesOrder;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false, precision = 10, scale = 3)
    private BigDecimal quantity;

    @Column(name = "unit_price_at_sale", nullable = false, precision = 12, scale = 2)
    private BigDecimal unitPriceAtSale; // Precio al momento de la venta

    @Column(name = "discount_per_item", precision = 12, scale = 2)
    @ColumnDefault("0.00")
    private BigDecimal discountPerItem = BigDecimal.ZERO;

    // El subtotal se calcula en la entidad o se puede usar la columna generada de la BD
    // Para JPA, es mejor calcularlo en Java o usar @Formula si es simple.
    // La columna GENERATED de tu DDL es para la BD, Hibernate no la actualiza directamente.
    // Vamos a calcularlo en el getter o al setear las cantidades/precios.
    @Transient // Para que JPA no intente mapearlo directamente si la BD ya lo calcula
    // O quita @Transient y calcúlalo en el setter/getter si la BD no es la única fuente
    private BigDecimal subtotal;


    public BigDecimal getSubtotal() {
        if (quantity != null && unitPriceAtSale != null) {
            BigDecimal itemDiscount = (discountPerItem != null) ? discountPerItem : BigDecimal.ZERO;
            return quantity.multiply(unitPriceAtSale.subtract(itemDiscount));
        }
        return BigDecimal.ZERO;
    }

}