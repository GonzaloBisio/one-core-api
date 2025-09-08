package com.one.core.application.dto.tenant.gym;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;


// --- Clase (template) + Sesiones ---
@Data
public class ClassTemplateCreateDTO {
    public Long classTypeId;
    public Long instructorId;
    public Long roomId;
    public String rrule;                   // p.ej. FREQ=WEEKLY;BYDAY=MO,WE,FR
    public LocalTime startTimeLocal;
    public Integer durationMinutes = 60;
    public Integer capacity;               // null => sala.default
    public LocalDate startDate;
    public LocalDate endDate;
    public Boolean active = true;
}