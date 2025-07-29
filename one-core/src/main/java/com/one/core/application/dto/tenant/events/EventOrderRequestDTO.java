package com.one.core.application.dto.tenant.events;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class EventOrderRequestDTO {
    private Long customerId;
    @NotNull
    @FutureOrPresent
    private LocalDate eventDate;
    private String notes;
    private String deliveryAddress;
    @NotEmpty
    private List<EventOrderItemRequestDTO> items;
}