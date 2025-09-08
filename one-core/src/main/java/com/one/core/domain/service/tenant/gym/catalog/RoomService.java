// src/main/java/com/one/core/domain/service/tenant/gym/catalog/RoomService.java
package com.one.core.domain.service.tenant.gym.catalog;

import com.one.core.application.dto.tenant.gym.RoomDTO;
import com.one.core.application.exception.ResourceNotFoundException;
import com.one.core.application.mapper.gym.catalog.RoomMapper;
import com.one.core.domain.model.tenant.gym.Room;
import com.one.core.domain.repository.tenant.gym.RoomRepository;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class RoomService {

    private final RoomRepository repo;
    private final RoomMapper mapper;

    public RoomService(RoomRepository repo, RoomMapper mapper) {
        this.repo = repo;
        this.mapper = mapper;
    }

    @Transactional
    public RoomDTO create(RoomDTO dto) {
        Room e = mapper.toNewEntity(dto);
        return mapper.toDTO(repo.save(e));
    }

    @Transactional
    public RoomDTO update(Long id, RoomDTO dto) {
        Room e = repo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Room","id", id));
        mapper.copy(dto, e);
        return mapper.toDTO(repo.save(e));
    }

    @Transactional
    public void setActive(Long id, boolean active) {
        Room e = repo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Room","id", id));
        e.setActive(active);
        repo.save(e);
    }

    @Transactional
    public RoomDTO get(Long id) {
        return repo.findById(id).map(mapper::toDTO)
                .orElseThrow(() -> new ResourceNotFoundException("Room","id", id));
    }

    @Transactional
    public Page<RoomDTO> list(Pageable pageable) {
        return repo.findAll(pageable).map(mapper::toDTO);
    }
}
