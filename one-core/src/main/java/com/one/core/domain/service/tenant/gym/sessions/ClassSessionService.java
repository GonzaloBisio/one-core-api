package com.one.core.domain.service.tenant.gym.sessions;

import com.one.core.application.dto.tenant.gym.ClassSessionCreateDTO;
import com.one.core.application.dto.tenant.gym.ClassSessionDTO;
import com.one.core.application.exception.ResourceNotFoundException;
import com.one.core.application.exception.ValidationException;
import com.one.core.application.mapper.gym.session.ClassSessionMapper;
import com.one.core.domain.model.enums.gym.SessionStatus;
import com.one.core.domain.model.tenant.gym.ClassSession;
import com.one.core.domain.model.tenant.gym.ClassTemplate;
import com.one.core.domain.repository.tenant.gym.ClassSessionRepository;
import com.one.core.domain.repository.tenant.gym.ClassTemplateRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class ClassSessionService {

    private final ClassSessionRepository sessionRepo;
    private final ClassTemplateRepository templateRepo;
    private final ClassSessionMapper mapper;

    public ClassSessionService(ClassSessionRepository sessionRepo,
                               ClassTemplateRepository templateRepo,
                               ClassSessionMapper mapper) {
        this.sessionRepo = sessionRepo;
        this.templateRepo = templateRepo;
        this.mapper = mapper;
    }

    @Transactional
    public ClassSessionDTO create(ClassSessionCreateDTO dto) {
        ClassTemplate template = templateRepo.findById(dto.getClassId())
                .orElseThrow(() -> new ResourceNotFoundException("ClassTemplate", "id", dto.getClassId()));

        if (!Boolean.TRUE.equals(template.isActive())) {
            throw new ValidationException("El template de clase está inactivo.");
        }

        ClassSession s = mapper.toNewEntity(dto);
        mapper.applyTemplate(s, template);

        // Completar endAt si viene null
        if (s.getEndAt() == null) {
            int minutes = (template.getDurationMinutes() != null) ? template.getDurationMinutes() : 60;
            LocalDateTime end = s.getStartAt().plusMinutes(minutes);
            s.setEndAt(end);
        }

        // Completar capacity si viene null
        if (s.getCapacity() == null) {
            if (template.getCapacity() != null) {
                s.setCapacity(template.getCapacity());
            } else if (template.getRoom() != null) {
                s.setCapacity(template.getRoom().getCapacityDefault());
            } else {
                s.setCapacity(20);
            }
        }

        return mapper.toDTO(sessionRepo.save(s));
    }

    @Transactional
    public void setStatus(Long sessionId, SessionStatus status) {
        ClassSession s = sessionRepo.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("ClassSession", "id", sessionId));

        // Reglas mínimas: DONE/CANCELLED sólo desde SCHEDULED
        if (status != SessionStatus.SCHEDULED && s.getStatus() != SessionStatus.SCHEDULED) {
            throw new ValidationException("Solo se puede cambiar a DONE/CANCELLED desde SCHEDULED.");
        }
        s.setStatus(status);
        sessionRepo.save(s);
    }
}
