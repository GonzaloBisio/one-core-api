package com.one.core.application.controller.tenant.gym.sessions;

import com.one.core.application.dto.tenant.gym.ClassSessionCreateDTO;
import com.one.core.application.dto.tenant.gym.ClassSessionDTO;
import com.one.core.domain.model.enums.gym.SessionStatus;
import com.one.core.domain.service.tenant.gym.sessions.ClassSessionService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/gym/sessions")
@PreAuthorize("hasAnyRole('TENANT_USER','TENANT_ADMIN','SUPER_ADMIN')")
public class ClassSessionController {

    private final ClassSessionService service;

    public ClassSessionController(ClassSessionService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<ClassSessionDTO> create(@Valid @RequestBody ClassSessionCreateDTO dto) {
        return ResponseEntity.ok(service.create(dto));
    }

    @PostMapping("/{id}/status/{status}")
    public ResponseEntity<Void> setStatus(@PathVariable Long id, @PathVariable SessionStatus status) {
        service.setStatus(id, status);
        return ResponseEntity.ok().build();
    }
}
