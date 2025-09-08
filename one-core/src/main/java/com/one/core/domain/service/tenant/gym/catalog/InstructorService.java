package com.one.core.domain.service.tenant.gym.catalog;

import com.one.core.application.dto.tenant.gym.InstructorDTO;
import com.one.core.application.exception.ResourceNotFoundException;
import com.one.core.application.mapper.gym.catalog.InstructorMapper;
import com.one.core.domain.model.tenant.gym.Instructor;
import com.one.core.domain.repository.tenant.gym.InstructorRepository;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class InstructorService {

    private final InstructorRepository repo;
    private final InstructorMapper mapper;

    public InstructorService(InstructorRepository repo, InstructorMapper mapper) {
        this.repo = repo;
        this.mapper = mapper;
    }

    @Transactional
    public InstructorDTO create(InstructorDTO dto) {
        Instructor e = mapper.toNewEntity(dto);
        return mapper.toDTO(repo.save(e));
    }

    @Transactional
    public InstructorDTO update(Long id, InstructorDTO dto) {
        Instructor e = repo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Instructor","id", id));
        mapper.copy(dto, e);
        return mapper.toDTO(repo.save(e));
    }

    @Transactional
    public void setActive(Long id, boolean active) {
        Instructor e = repo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Instructor","id", id));
        e.setActive(active);
        repo.save(e);
    }

    @Transactional
    public InstructorDTO get(Long id) {
        return repo.findById(id).map(mapper::toDTO)
                .orElseThrow(() -> new ResourceNotFoundException("Instructor","id", id));
    }

    @Transactional
    public Page<InstructorDTO> list(Pageable pageable) {
        return repo.findAll(pageable).map(mapper::toDTO);
    }
}
