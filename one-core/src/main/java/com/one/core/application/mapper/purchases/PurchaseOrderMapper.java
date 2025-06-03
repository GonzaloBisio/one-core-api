package com.one.core.application.mapper.purchases;

import com.one.core.application.dto.tenant.purchases.PurchaseOrderDTO;
import com.one.core.application.dto.tenant.purchases.PurchaseOrderItemDTO;
import com.one.core.application.dto.tenant.purchases.PurchaseOrderItemRequestDTO;
import com.one.core.domain.model.tenant.product.Product;
import com.one.core.domain.model.tenant.purchases.PurchaseOrder;
import com.one.core.domain.model.tenant.purchases.PurchaseOrderItem;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.stream.Collectors;

@Component
public class PurchaseOrderMapper {

    public PurchaseOrderItemDTO toDTO(PurchaseOrderItem entity) {
        if (entity == null) return null;
        PurchaseOrderItemDTO dto = new PurchaseOrderItemDTO();
        dto.setId(entity.getId());
        if (entity.getProduct() != null) {
            dto.setProductId(entity.getProduct().getId());
            dto.setProductName(entity.getProduct().getName());
        }
        dto.setQuantityOrdered(entity.getQuantityOrdered());
        dto.setQuantityReceived(entity.getQuantityReceived());
        dto.setUnitPrice(entity.getUnitPrice());
        dto.setSubtotal(entity.getSubtotal());
        return dto;
    }

    public PurchaseOrderDTO toDTO(PurchaseOrder entity) {
        if (entity == null) return null;
        PurchaseOrderDTO dto = new PurchaseOrderDTO();
        dto.setId(entity.getId());
        if (entity.getSupplier() != null) {
            dto.setSupplierId(entity.getSupplier().getId());
            dto.setSupplierName(entity.getSupplier().getName());
        }
        dto.setOrderDate(entity.getOrderDate());
        dto.setExpectedDeliveryDate(entity.getExpectedDeliveryDate());
        dto.setStatus(entity.getStatus());
        dto.setTotalAmount(entity.getTotalAmount());
        dto.setNotes(entity.getNotes());
        if (entity.getCreatedByUser() != null) {
            dto.setCreatedByUserId(entity.getCreatedByUser().getId());
            dto.setCreatedByUsername(entity.getCreatedByUser().getUsername());
        }
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        dto.setItems(entity.getItems() != null ?
                entity.getItems().stream().map(this::toDTO).collect(Collectors.toList()) :
                Collections.emptyList());
        return dto;
    }

    public PurchaseOrderItem itemRequestDtoToEntity(PurchaseOrderItemRequestDTO itemDto, Product product) {
        PurchaseOrderItem item = new PurchaseOrderItem();
        item.setProduct(product);
        item.setQuantityOrdered(itemDto.getQuantityOrdered());
        item.setUnitPrice(itemDto.getUnitPrice());
        item.setQuantityReceived(BigDecimal.ZERO); // Inicialmente nada recibido
        // El subtotal se calcula o se deja que la BD lo haga si es generated column
        return item;
    }
}