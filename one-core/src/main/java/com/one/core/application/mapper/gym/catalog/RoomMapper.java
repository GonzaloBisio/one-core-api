// src/main/java/com/one/core/application/mapper/gym/catalog/RoomMapper.java
package com.one.core.application.mapper.gym.catalog;

import com.one.core.application.dto.tenant.gym.RoomDTO;
import com.one.core.domain.model.tenant.gym.Room;
import org.springframework.stereotype.Component;

@Component
public class RoomMapper {

    public Room toNewEntity(RoomDTO dto) {
        Room e = new Room();
        e.setName(dto.getName());
        e.setLocation(dto.getLocation());
        if (dto.getCapacityDefault() != null) {
            e.setCapacityDefault(dto.getCapacityDefault());
        }
        e.setActive(dto.getActive() != null ? dto.getActive() : true);
        return e;
    }

    public void copy(RoomDTO dto, Room target) {
        if (dto.getName() != null)            target.setName(dto.getName());
        if (dto.getLocation() != null)        target.setLocation(dto.getLocation());
        if (dto.getCapacityDefault() != null) target.setCapacityDefault(dto.getCapacityDefault());
        if (dto.getActive() != null)          target.setActive(dto.getActive());
    }

    public RoomDTO toDTO(Room e) {
        RoomDTO dto = new RoomDTO();
        dto.setId(e.getId());
        dto.setName(e.getName());
        dto.setLocation(e.getLocation());
        dto.setCapacityDefault(e.getCapacityDefault());
        dto.setActive(e.isActive());
        return dto;
    }
}
