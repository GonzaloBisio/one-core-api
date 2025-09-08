// src/main/java/com/one/core/application/mapper/gym/catalog/ClassTypeMapper.java
package com.one.core.application.mapper.gym.catalog;

import com.one.core.application.dto.tenant.gym.ClassTypeDTO;
import com.one.core.domain.model.tenant.gym.ClassType;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
public class ClassTypeMapper {

    /**
     * Crea entidad nueva desde DTO (con defaults).
     */
    public ClassType toNewEntity(ClassTypeDTO dto) {
        ClassType e = new ClassType();
        e.setName(dto.getName());
        e.setDescription(dto.getDescription());
        if (dto.getTags() != null) {
            e.setTags(dto.getTags().toArray(String[]::new));
        }
        e.setActive(dto.getActive() != null ? dto.getActive() : true);
        return e;
    }

    /**
     * Copia solo campos no nulos desde el DTO.
     */
    public void copy(ClassTypeDTO dto, ClassType target) {
        if (dto.getName() != null) target.setName(dto.getName());
        if (dto.getDescription() != null) target.setDescription(dto.getDescription());
        if (dto.getTags() != null) target.setTags(dto.getTags().toArray(String[]::new));
        if (dto.getActive() != null) target.setActive(dto.getActive());
    }

    /**
     * Entidad -> DTO.
     */
    public ClassTypeDTO toDTO(ClassType e) {
        ClassTypeDTO dto = new ClassTypeDTO();
        dto.setId(e.getId());
        dto.setName(e.getName());
        dto.setDescription(e.getDescription());
        if (e.getTags() != null) {
            dto.setTags(Arrays.asList(e.getTags()));
        }
        dto.setActive(e.isActive());
        return dto;
    }
}
