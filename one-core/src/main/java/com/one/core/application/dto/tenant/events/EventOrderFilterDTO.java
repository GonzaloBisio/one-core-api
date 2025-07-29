package com.one.core.application.dto.tenant.events;

import lombok.Data;
import java.time.LocalDate;

@Data
public class EventOrderFilterDTO {
    private Long customerId;
    private String status; // PENDING, CONFIRMED, etc.
    private LocalDate eventDateFrom;
    private LocalDate eventDateTo;
}