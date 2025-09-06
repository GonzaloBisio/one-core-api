package com.one.core.application.mapper.sales;

import com.one.core.application.dto.tenant.sales.SalesOrderDTO;
import com.one.core.application.dto.tenant.sales.SalesOrderItemDTO;
import com.one.core.application.dto.tenant.sales.SalesOrderItemRequestDTO;
import com.one.core.domain.model.tenant.sales.SalesOrder;
import com.one.core.domain.model.tenant.sales.SalesOrderItem;
import com.one.core.domain.model.tenant.product.Product; // Para referencia al crear SalesOrderItem
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.stream.Collectors;

@Component
public class SalesOrderMapper {

    public SalesOrderItemDTO toDTO(SalesOrderItem entity) {
        if (entity == null) return null;
        SalesOrderItemDTO dto = new SalesOrderItemDTO();
        dto.setId(entity.getId());
        if (entity.getProduct() != null) {
            dto.setProductId(entity.getProduct().getId());
            dto.setProductName(entity.getProduct().getName());
        }
        dto.setQuantity(entity.getQuantity());
        dto.setUnitPriceAtSale(entity.getUnitPriceAtSale());
        dto.setDiscountPerItem(entity.getDiscountPerItem());
        dto.setSubtotal(entity.getSubtotal()); // Llama al getter que calcula
        return dto;
    }

    public SalesOrderDTO toDTO(SalesOrder entity) {
        if (entity == null) return null;
        SalesOrderDTO dto = new SalesOrderDTO();
        dto.setId(entity.getId());
        if (entity.getCustomer() != null) {
            dto.setCustomerId(entity.getCustomer().getId());
            dto.setCustomerName(entity.getCustomer().getName());
        }
        dto.setOrderDate(entity.getOrderDate());
        dto.setStatus(entity.getStatus());
        dto.setSubtotalAmount(entity.getSubtotalAmount());
        dto.setTaxAmount(entity.getTaxAmount());
        dto.setDiscountAmount(entity.getDiscountAmount());
        dto.setTotalAmount(entity.getTotalAmount());
        dto.setPaymentMethod(entity.getPaymentMethod());
        dto.setShippingAddress(entity.getShippingAddress());
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

    // Este método ayuda a crear la entidad SalesOrderItem a partir del DTO de request y el Product ya cargado
    public SalesOrderItem itemRequestDtoToEntity(SalesOrderItemRequestDTO itemDto, Product product) {
        SalesOrderItem item = new SalesOrderItem();
        item.setProduct(product);
        item.setQuantity(itemDto.getQuantity());
        item.setUnitPriceAtSale(itemDto.getUnitPrice()); // El DTO lo llama unitPrice
        item.setDiscountPerItem(itemDto.getDiscountPerItem() != null ? itemDto.getDiscountPerItem() : BigDecimal.ZERO);
        item.setSkipAutoPackaging(Boolean.TRUE.equals(itemDto.getSkipAutoPackaging()));
        // El subtotal se calcula o se deja que la BD lo haga si es generated column
        return item;
    }
}