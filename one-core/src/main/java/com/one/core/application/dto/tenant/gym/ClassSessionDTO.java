package com.one.core.application.dto.tenant.gym;

import com.one.core.domain.model.enums.gym.SessionStatus;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ClassSessionDTO {
    public Long id;
    /** ID del template (classes.id). */
    public Long classId;

    public LocalDateTime startAt;
    public LocalDateTime endAt;

    public SessionStatus status;
    public Integer capacity;
    public Integer bookedCount;

    // Info derivada (opcional, para UI)
    public String classTypeName;
    public String instructorName;
    public String roomName;
}