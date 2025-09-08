package com.one.core.application.controller.tenant.gym.catalog;

import com.one.core.application.dto.tenant.gym.RoomDTO;
import com.one.core.application.dto.tenant.response.PageableResponse;
import com.one.core.domain.service.tenant.gym.catalog.RoomService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/gym/catalog/rooms")
@PreAuthorize("hasAnyRole('TENANT_USER','TENANT_ADMIN','SUPER_ADMIN')")
public class RoomController {

    private final RoomService service;

    public RoomController(RoomService service) { this.service = service; }

    @PostMapping
    @PreAuthorize("hasAnyRole('TENANT_ADMIN','SUPER_ADMIN')")
    public ResponseEntity<RoomDTO> create(@Valid @RequestBody RoomDTO dto) {
        return ResponseEntity.ok(service.create(dto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN','SUPER_ADMIN')")
    public ResponseEntity<RoomDTO> update(@PathVariable Long id, @Valid @RequestBody RoomDTO dto) {
        return ResponseEntity.ok(service.update(id, dto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<RoomDTO> get(@PathVariable Long id) { return ResponseEntity.ok(service.get(id)); }

    @GetMapping
    public ResponseEntity<PageableResponse<RoomDTO>> list(@PageableDefault Pageable pageable) {
        Page<RoomDTO> page = service.list(pageable);
        return ResponseEntity.ok(new PageableResponse<>(page));
    }

    @PostMapping("/{id}/activate")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN','SUPER_ADMIN')")
    public ResponseEntity<Void> activate(@PathVariable Long id) { service.setActive(id, true); return ResponseEntity.ok().build(); }

    @PostMapping("/{id}/deactivate")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN','SUPER_ADMIN')")
    public ResponseEntity<Void> deactivate(@PathVariable Long id) { service.setActive(id, false); return ResponseEntity.ok().build(); }
}
