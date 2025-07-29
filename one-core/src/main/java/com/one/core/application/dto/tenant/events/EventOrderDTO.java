package com.one.core.application.dto.tenant.events;

import com.one.core.domain.model.enums.events.EventOrderStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

@Data
public class EventOrderDTO {
    private Long id;
    private Long customerId;
    private String customerName;
    private LocalDate eventDate;
    private EventOrderStatus status;
    private String notes;
    private BigDecimal totalAmount;
    private String deliveryAddress;
    private Long createdByUserId;
    private String createdByUsername;
    private OffsetDateTime createdAt;
    private List<EventOrderItemDTO> items;
}