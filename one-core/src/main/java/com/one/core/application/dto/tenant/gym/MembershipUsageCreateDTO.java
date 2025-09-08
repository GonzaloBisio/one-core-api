package com.one.core.application.dto.tenant.gym;

import com.one.core.domain.model.enums.gym.UsageReason;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class MembershipUsageCreateDTO {
    private Long sessionId;                    // opcional

    private LocalDate eventDate;               // opcional, default hoy en service

    @NotNull
    private UsageReason reason;

    @NotNull @Min(1)
    private Integer units = 1;
}