// src/main/java/com/one/core/application/mapper/gym/catalog/InstructorMapper.java
package com.one.core.application.mapper.gym.catalog;

import com.one.core.application.dto.tenant.gym.InstructorDTO;
import com.one.core.domain.model.tenant.gym.Instructor;
import org.springframework.stereotype.Component;

@Component
public class InstructorMapper {

    public Instructor toNewEntity(InstructorDTO dto) {
        Instructor e = new Instructor();
        e.setName(dto.getName());
        e.setEmail(dto.getEmail());
        e.setPhone(dto.getPhone());
        e.setBio(dto.getBio());
        e.setActive(dto.getActive() != null ? dto.getActive() : true);
        return e;
    }

    public void copy(InstructorDTO dto, Instructor target) {
        if (dto.getName() != null)   target.setName(dto.getName());
        if (dto.getEmail() != null)  target.setEmail(dto.getEmail());
        if (dto.getPhone() != null)  target.setPhone(dto.getPhone());
        if (dto.getBio() != null)    target.setBio(dto.getBio());
        if (dto.getActive() != null) target.setActive(dto.getActive());
    }

    public InstructorDTO toDTO(Instructor e) {
        InstructorDTO dto = new InstructorDTO();
        dto.setId(e.getId());
        dto.setName(e.getName());
        dto.setEmail(e.getEmail());
        dto.setPhone(e.getPhone());
        dto.setBio(e.getBio());
        dto.setActive(e.isActive());
        return dto;
    }
}
