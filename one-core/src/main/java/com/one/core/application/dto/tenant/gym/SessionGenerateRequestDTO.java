package com.one.core.application.dto.tenant.gym;

import lombok.Data;

import java.time.LocalDate;

@Data
public class SessionGenerateRequestDTO {
    public Long classId;
    public LocalDate from;  // inclusive
    public LocalDate to;    // inclusive
}