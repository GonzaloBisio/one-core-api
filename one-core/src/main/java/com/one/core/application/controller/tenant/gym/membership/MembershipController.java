package com.one.core.application.controller.tenant.gym.membership;

import com.one.core.application.dto.tenant.gym.MembershipCreateDTO;
import com.one.core.application.dto.tenant.gym.MembershipDTO;
import com.one.core.application.dto.tenant.response.PageableResponse;
import com.one.core.domain.service.tenant.gym.membership.MembershipService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/gym/memberships")
@PreAuthorize("hasAnyRole('TENANT_USER','TENANT_ADMIN','SUPER_ADMIN')")
public class MembershipController {

    private final MembershipService service;

    public MembershipController(MembershipService service) { this.service = service; }

    @PostMapping
    @PreAuthorize("hasAnyRole('TENANT_ADMIN','SUPER_ADMIN')")
    public ResponseEntity<MembershipDTO> create(@Valid @RequestBody MembershipCreateDTO dto) {
        return ResponseEntity.ok(service.create(dto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<MembershipDTO> get(@PathVariable Long id) { return ResponseEntity.ok(service.get(id)); }

    @GetMapping
    public ResponseEntity<PageableResponse<MembershipDTO>> list(
            @RequestParam(required = false) Long customerId,
            @PageableDefault Pageable pageable) {
        Page<MembershipDTO> page = service.list(customerId, pageable);
        return ResponseEntity.ok(new PageableResponse<>(page));
    }

    @PostMapping("/{id}/pause")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN','SUPER_ADMIN')")
    public ResponseEntity<Void> pause(
            @PathVariable Long id,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate until) {
        service.pause(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/resume")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN','SUPER_ADMIN')")
    public ResponseEntity<Void> resume(@PathVariable Long id) {
        service.resume(id); return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN','SUPER_ADMIN')")
    public ResponseEntity<Void> cancel(@PathVariable Long id) {
        service.cancel(id); return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/change-plan/{planId}")
    @PreAuthorize("hasAnyRole('TENANT_ADMIN','SUPER_ADMIN')")
    public ResponseEntity<MembershipDTO> changePlan(@PathVariable Long id, @PathVariable Long planId) {
        return ResponseEntity.ok(service.changePlan(id, planId));
    }
}
