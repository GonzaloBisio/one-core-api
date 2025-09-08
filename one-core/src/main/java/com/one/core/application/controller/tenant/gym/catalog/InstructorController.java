package com.one.core.application.controller.tenant.gym.catalog;

import com.one.core.application.dto.tenant.gym.InstructorDTO;
import com.one.core.application.dto.tenant.response.PageableResponse;
import com.one.core.domain.service.tenant.gym.catalog.InstructorService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/gym/catalog/instructors")
@PreAuthorize("hasAnyRole('TENANT_USER','TENANT_ADMIN','SUPER_ADMIN')")
public class InstructorController {

    private final InstructorService service;

    public InstructorController(InstructorService service) { this.service = service; }

    @PostMapping
    @PreAuthorize("hasAnyRole('TENANT_ADMIN','SUPER_ADMIN')")
    public ResponseEntity<InstructorDTO> create(@Valid @RequestBody InstructorDTO dto) {
        return ResponseEntity.ok(service.create(dto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN','SUPER_ADMIN')")
    public ResponseEntity<InstructorDTO> update(@PathVariable Long id, @Valid @RequestBody InstructorDTO dto) {
        return ResponseEntity.ok(service.update(id, dto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<InstructorDTO> get(@PathVariable Long id) { return ResponseEntity.ok(service.get(id)); }

    @GetMapping
    public ResponseEntity<PageableResponse<InstructorDTO>> list(@PageableDefault Pageable pageable) {
        Page<InstructorDTO> page = service.list(pageable);
        return ResponseEntity.ok(new PageableResponse<>(page));
    }

    @PostMapping("/{id}/activate")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN','SUPER_ADMIN')")
    public ResponseEntity<Void> activate(@PathVariable Long id) { service.setActive(id, true); return ResponseEntity.ok().build(); }

    @PostMapping("/{id}/deactivate")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN','SUPER_ADMIN')")
    public ResponseEntity<Void> deactivate(@PathVariable Long id) { service.setActive(id, false); return ResponseEntity.ok().build(); }
}
