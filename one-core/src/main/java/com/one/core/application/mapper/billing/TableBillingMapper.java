package com.one.core.application.mapper.billing;

import com.one.core.application.dto.billing.TableCheckDTO;
import com.one.core.application.dto.billing.TableCheckItemDTO;
import com.one.core.application.dto.billing.TableCheckItemRequestDTO;
import com.one.core.domain.model.tenant.product.Product;
import com.one.core.domain.model.tenant.sales.SalesOrder;
import com.one.core.domain.model.tenant.sales.SalesOrderItem;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.stream.Collectors;

/**
 * Mapper to convert between billing DTOs and sales entities.
 */
@Component
public class TableBillingMapper {

    public SalesOrderItem toEntity(TableCheckItemRequestDTO dto) {
        if (dto == null) return null;
        SalesOrderItem item = new SalesOrderItem();
        Product product = new Product();
        product.setId(dto.getProductId());
        item.setProduct(product);
        item.setQuantity(dto.getQuantity());
        item.setUnitPriceAtSale(dto.getUnitPrice());
        item.setDiscountPerItem(dto.getDiscountPerItem() != null ? dto.getDiscountPerItem() : BigDecimal.ZERO);
        return item;
    }

    public TableCheckItemDTO toDTO(SalesOrderItem entity) {
        if (entity == null) return null;
        TableCheckItemDTO dto = new TableCheckItemDTO();
        dto.setId(entity.getId());
        if (entity.getProduct() != null) {
            dto.setProductId(entity.getProduct().getId());
            dto.setProductName(entity.getProduct().getName());
        }
        dto.setQuantity(entity.getQuantity());
        dto.setUnitPriceAtSale(entity.getUnitPriceAtSale());
        dto.setDiscountPerItem(entity.getDiscountPerItem());
        dto.setSubtotal(entity.getSubtotal());
        return dto;
    }

    public TableCheckDTO toDTO(SalesOrder entity) {
        if (entity == null) return null;
        TableCheckDTO dto = new TableCheckDTO();
        dto.setId(entity.getId());
        dto.setTotalAmount(entity.getTotalAmount());
        dto.setItems(entity.getItems() != null ?
                entity.getItems().stream().map(this::toDTO).collect(Collectors.toList()) :
                Collections.emptyList());
        return dto;
    }
}

