package com.one.core.application.dto.tenant.gym;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ClassSessionCreateDTO {
    @NotNull
    private Long classId;

    @NotNull
    private LocalDateTime startAt;

    private LocalDateTime endAt;

    private Integer capacity;
}