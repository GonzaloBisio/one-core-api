// src/main/java/com/one/core/domain/service/tenant/gym/catalog/ClassTypeService.java
package com.one.core.domain.service.tenant.gym.catalog;

import com.one.core.application.dto.tenant.gym.ClassTypeDTO;
import com.one.core.application.exception.ResourceNotFoundException;
import com.one.core.application.mapper.gym.catalog.ClassTypeMapper;
import com.one.core.domain.model.tenant.gym.ClassType;
import com.one.core.domain.repository.tenant.gym.ClassTypeRepository;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class ClassTypeService {

    private final ClassTypeRepository repo;
    private final ClassTypeMapper mapper;

    public ClassTypeService(ClassTypeRepository repo, ClassTypeMapper mapper) {
        this.repo = repo;
        this.mapper = mapper;
    }

    @Transactional
    public ClassTypeDTO create(ClassTypeDTO dto) {
        ClassType e = mapper.toNewEntity(dto);
        return mapper.toDTO(repo.save(e));
    }

    @Transactional
    public ClassTypeDTO update(Long id, ClassTypeDTO dto) {
        ClassType e = repo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ClassType","id", id));
        mapper.copy(dto, e);
        return mapper.toDTO(repo.save(e));
    }

    @Transactional
    public void setActive(Long id, boolean active) {
        ClassType e = repo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ClassType","id", id));
        e.setActive(active);
        repo.save(e);
    }

    @Transactional
    public ClassTypeDTO get(Long id) {
        return repo.findById(id).map(mapper::toDTO)
                .orElseThrow(() -> new ResourceNotFoundException("ClassType","id", id));
    }

    @Transactional
    public Page<ClassTypeDTO> list(Pageable pageable) {
        return repo.findAll(pageable).map(mapper::toDTO);
    }
}
