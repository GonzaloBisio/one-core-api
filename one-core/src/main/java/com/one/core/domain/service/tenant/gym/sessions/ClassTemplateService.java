package com.one.core.domain.service.tenant.gym.sessions;

import com.one.core.application.dto.tenant.gym.ClassTemplateCreateDTO;
import com.one.core.application.dto.tenant.gym.ClassTemplateDTO;
import com.one.core.application.exception.ResourceNotFoundException;
import com.one.core.application.mapper.gym.session.ClassTemplateMapper;
import com.one.core.domain.model.tenant.gym.ClassTemplate;
import com.one.core.domain.model.tenant.gym.ClassType;
import com.one.core.domain.model.tenant.gym.Instructor;
import com.one.core.domain.model.tenant.gym.Room;
import com.one.core.domain.repository.tenant.gym.ClassTemplateRepository;
import com.one.core.domain.repository.tenant.gym.ClassTypeRepository;
import com.one.core.domain.repository.tenant.gym.InstructorRepository;
import com.one.core.domain.repository.tenant.gym.RoomRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ClassTemplateService {

    private final ClassTemplateRepository repo;
    private final ClassTypeRepository classTypeRepo;
    private final RoomRepository roomRepo;
    private final InstructorRepository instructorRepo;
    private final ClassTemplateMapper mapper;

    public ClassTemplateService(
            ClassTemplateRepository repo,
            ClassTypeRepository classTypeRepo,
            RoomRepository roomRepo,
            InstructorRepository instructorRepo,
            ClassTemplateMapper mapper
    ) {
        this.repo = repo;
        this.classTypeRepo = classTypeRepo;
        this.roomRepo = roomRepo;
        this.instructorRepo = instructorRepo;
        this.mapper = mapper;
    }

    @Transactional
    public ClassTemplateDTO create(ClassTemplateCreateDTO dto) {
        // Asociaciones requeridas
        ClassType type = classTypeRepo.findById(dto.getClassTypeId())
                .orElseThrow(() -> new ResourceNotFoundException("ClassType", "id", dto.getClassTypeId()));
        Room room = roomRepo.findById(dto.getRoomId())
                .orElseThrow(() -> new ResourceNotFoundException("Room", "id", dto.getRoomId()));

        // Asociaciones opcionales
        Instructor instructor = null;
        if (dto.getInstructorId() != null) {
            instructor = instructorRepo.findById(dto.getInstructorId())
                    .orElseThrow(() -> new ResourceNotFoundException("Instructor", "id", dto.getInstructorId()));
        }

        // Entidad base + asociaciones
        ClassTemplate e = mapper.toNewEntity(dto);
        mapper.applyAssociations(e, type, instructor, room);

        // Si no se envía capacity, usar default de la sala
        if (e.getCapacity() == null) {
            e.setCapacity(room.getCapacityDefault());
        }

        return mapper.toDTO(repo.save(e));
    }

    @Transactional
    public ClassTemplateDTO update(Long id, ClassTemplateCreateDTO dto) {
        ClassTemplate e = repo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ClassTemplate", "id", id));

        // Actualizar asociaciones si vienen en el DTO
        ClassType type = e.getClassType();
        if (dto.getClassTypeId() != null) {
            type = classTypeRepo.findById(dto.getClassTypeId())
                    .orElseThrow(() -> new ResourceNotFoundException("ClassType", "id", dto.getClassTypeId()));
            e.setClassType(type);
        }

        Room room = e.getRoom();
        if (dto.getRoomId() != null) {
            room = roomRepo.findById(dto.getRoomId())
                    .orElseThrow(() -> new ResourceNotFoundException("Room", "id", dto.getRoomId()));
            e.setRoom(room);
        }

        if (dto.getInstructorId() != null) {
            // Si te interesa permitir “quitar” el instructor, podés usar un flag explícito en el DTO.
            Instructor instructor = instructorRepo.findById(dto.getInstructorId())
                    .orElseThrow(() -> new ResourceNotFoundException("Instructor", "id", dto.getInstructorId()));
            e.setInstructor(instructor);
        }

        // Copiar campos simples desde el DTO -> entidad (orden correcto)
        mapper.copyFieldsForUpdate(e, dto);

        // Si cambiaron de sala y NO enviaron capacity explícito, usar el default de la nueva sala
        if (dto.getRoomId() != null && dto.getCapacity() == null) {
            e.setCapacity(room.getCapacityDefault());
        }

        return mapper.toDTO(repo.save(e));
    }

    @Transactional
    public void setActive(Long id, boolean active) {
        ClassTemplate e = repo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ClassTemplate", "id", id));
        e.setActive(active);
        repo.save(e);
    }

    @Transactional(readOnly = true)
    public ClassTemplateDTO get(Long id) {
        return repo.findById(id)
                .map(mapper::toDTO)
                .orElseThrow(() -> new ResourceNotFoundException("ClassTemplate", "id", id));
    }

    @Transactional(readOnly = true)
    public Page<ClassTemplateDTO> list(Pageable pageable) {
        return repo.findAll(pageable).map(mapper::toDTO);
    }
}
