package com.one.core.application.mapper.events;

import com.one.core.application.dto.tenant.events.EventOrderDTO;
import com.one.core.application.dto.tenant.events.EventOrderItemDTO;
import com.one.core.domain.model.tenant.events.EventOrder;
import com.one.core.domain.model.tenant.events.EventOrderItem;
import org.springframework.stereotype.Component;
import java.util.stream.Collectors;

@Component
public class EventOrderMapper {
    public EventOrderDTO toDTO(EventOrder entity) {
        if (entity == null) return null;

        EventOrderDTO dto = new EventOrderDTO();
        dto.setId(entity.getId());
        dto.setEventDate(entity.getEventDate());
        dto.setStatus(entity.getStatus());
        dto.setNotes(entity.getNotes());
        dto.setTotalAmount(entity.getTotalAmount());
        dto.setDeliveryAddress(entity.getDeliveryAddress());
        dto.setCreatedAt(entity.getCreatedAt());

        if (entity.getCustomer() != null) {
            dto.setCustomerId(entity.getCustomer().getId());
            dto.setCustomerName(entity.getCustomer().getName());
        }

        if (entity.getCreatedByUser() != null) {
            dto.setCreatedByUserId(entity.getCreatedByUser().getId());
            dto.setCreatedByUsername(entity.getCreatedByUser().getUsername());
        }

        dto.setItems(entity.getItems().stream().map(this::itemToDTO).collect(Collectors.toList()));
        return dto;
    }

    private EventOrderItemDTO itemToDTO(EventOrderItem itemEntity) {
        EventOrderItemDTO itemDto = new EventOrderItemDTO();
        itemDto.setId(itemEntity.getId());
        itemDto.setQuantity(itemEntity.getQuantity());
        itemDto.setUnitPrice(itemEntity.getUnitPrice());
        itemDto.setSubtotal(itemEntity.getSubtotal());

        if (itemEntity.getProduct() != null) {
            itemDto.setProductId(itemEntity.getProduct().getId());
            itemDto.setProductName(itemEntity.getProduct().getName());
        }
        return itemDto;
    }
}