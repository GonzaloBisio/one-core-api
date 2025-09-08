package com.one.core.application.mapper.gym.session;

import com.one.core.application.dto.tenant.gym.ClassSessionCreateDTO;
import com.one.core.application.dto.tenant.gym.ClassSessionDTO;
import com.one.core.domain.model.enums.gym.SessionStatus;
import com.one.core.domain.model.tenant.gym.ClassSession;
import com.one.core.domain.model.tenant.gym.ClassTemplate;
import org.springframework.stereotype.Component;

@Component
public class ClassSessionMapper {

    /** Construye una entidad nueva a partir del CreateDTO (sin asociaciones). */
    public ClassSession toNewEntity(ClassSessionCreateDTO dto) {
        ClassSession s = new ClassSession();
        s.setStatus(SessionStatus.SCHEDULED);
        s.setStartAt(dto.getStartAt());
        s.setEndAt(dto.getEndAt());      // el service puede calcularlo si viene null
        s.setCapacity(dto.getCapacity());
        s.setBookedCount(0);
        return s;
    }

    /** Aplica la asociación con el template de clase. */
    public void applyTemplate(ClassSession target, ClassTemplate template) {
        target.setClassTemplate(template);
    }

    /** Mapea entidad -> DTO de salida. */
    public ClassSessionDTO toDTO(ClassSession s) {
        ClassSessionDTO dto = new ClassSessionDTO();
        dto.setId(s.getId());
        dto.setStartAt(s.getStartAt());
        dto.setEndAt(s.getEndAt());
        dto.setStatus(s.getStatus());
        dto.setCapacity(s.getCapacity());
        dto.setBookedCount(s.getBookedCount());

        if (s.getClassTemplate() != null) {
            dto.setClassId(s.getClassTemplate().getId());
            if (s.getClassTemplate().getClassType() != null) {
                dto.setClassTypeName(s.getClassTemplate().getClassType().getName());
            }
            if (s.getClassTemplate().getInstructor() != null) {
                dto.setInstructorName(s.getClassTemplate().getInstructor().getName());
            }
            if (s.getClassTemplate().getRoom() != null) {
                dto.setRoomName(s.getClassTemplate().getRoom().getName());
            }
        }
        return dto;
    }
}
