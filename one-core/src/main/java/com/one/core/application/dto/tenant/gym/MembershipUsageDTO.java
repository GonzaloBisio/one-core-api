package com.one.core.application.dto.tenant.gym;

import com.one.core.domain.model.enums.gym.UsageReason;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class MembershipUsageDTO {
    private Long id;
    private Long membershipId;
    private Long sessionId;           // puede ser null
    private LocalDate eventDate;
    private Integer units;
    private UsageReason reason;
    private LocalDateTime createdAt;
}