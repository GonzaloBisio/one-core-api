package com.one.core.application.mapper.gym.session;

import com.one.core.application.dto.tenant.gym.ClassTemplateCreateDTO;
import com.one.core.application.dto.tenant.gym.ClassTemplateDTO;
import com.one.core.domain.model.tenant.gym.ClassTemplate;
import com.one.core.domain.model.tenant.gym.ClassType;
import com.one.core.domain.model.tenant.gym.Instructor;
import com.one.core.domain.model.tenant.gym.Room;
import org.springframework.stereotype.Component;

@Component
public class ClassTemplateMapper {

    /** Entidad nueva SIN asociaciones (type/instructor/room las setea el service). */
    public ClassTemplate toNewEntity(ClassTemplateCreateDTO dto) {
        ClassTemplate t = new ClassTemplate();
        t.setRrule(dto.getRrule());
        t.setStartTimeLocal(dto.getStartTimeLocal());
        t.setDurationMinutes(dto.getDurationMinutes() != null ? dto.getDurationMinutes() : 60);
        t.setCapacity(dto.getCapacity());
        t.setStartDate(dto.getStartDate());
        t.setEndDate(dto.getEndDate());
        t.setActive(dto.getActive() != null ? dto.getActive() : true);
        return t;
    }

    /** Aplica asociaciones resueltas por el service. */
    public void applyAssociations(ClassTemplate target,
                                  ClassType classType,
                                  Instructor instructor,
                                  Room room) {
        target.setClassType(classType);
        target.setInstructor(instructor);
        target.setRoom(room);
    }

    /** Update “parcial” usando el mismo CreateDTO (si usás PATCH/PUT con este DTO). */
    public void copyFieldsForUpdate(ClassTemplate target, ClassTemplateCreateDTO dto) {
        if (dto.getRrule() != null) target.setRrule(dto.getRrule());
        if (dto.getStartTimeLocal() != null) target.setStartTimeLocal(dto.getStartTimeLocal());
        if (dto.getDurationMinutes() != null) target.setDurationMinutes(dto.getDurationMinutes());
        if (dto.getCapacity() != null) target.setCapacity(dto.getCapacity());
        if (dto.getStartDate() != null) target.setStartDate(dto.getStartDate());
        if (dto.getEndDate() != null) target.setEndDate(dto.getEndDate());
        if (dto.getActive() != null) target.setActive(dto.getActive());
    }

    /** Entidad -> DTO. */
    public ClassTemplateDTO toDTO(ClassTemplate t) {
        ClassTemplateDTO dto = new ClassTemplateDTO();
        dto.setId(t.getId());
        dto.setRrule(t.getRrule());
        dto.setStartTimeLocal(t.getStartTimeLocal());
        dto.setDurationMinutes(t.getDurationMinutes());
        dto.setCapacity(t.getCapacity());
        dto.setStartDate(t.getStartDate());
        dto.setEndDate(t.getEndDate());
        dto.setActive(t.isActive());

        if (t.getClassType() != null)   dto.setClassTypeId(t.getClassType().getId());
        if (t.getInstructor() != null)  dto.setInstructorId(t.getInstructor().getId());
        if (t.getRoom() != null)        dto.setRoomId(t.getRoom().getId());
        return dto;
    }
}
