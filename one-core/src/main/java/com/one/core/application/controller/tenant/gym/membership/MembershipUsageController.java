package com.one.core.application.controller.tenant.gym.membership;

import com.one.core.application.dto.tenant.gym.MembershipUsageCreateDTO;
import com.one.core.application.dto.tenant.gym.MembershipUsageDTO;
import com.one.core.application.dto.tenant.response.PageableResponse;
import com.one.core.domain.service.tenant.gym.membership.MembershipUsageService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/gym/memberships/{membershipId}/usage")
@PreAuthorize("hasAnyRole('TENANT_USER','TENANT_ADMIN','SUPER_ADMIN')")
public class MembershipUsageController {

    private final MembershipUsageService service;

    public MembershipUsageController(MembershipUsageService service) { this.service = service; }

    @PostMapping
    @PreAuthorize("hasAnyRole('TENANT_ADMIN','SUPER_ADMIN')")
    public ResponseEntity<MembershipUsageDTO> addUsage(
            @PathVariable Long membershipId,
            @Valid @RequestBody MembershipUsageCreateDTO dto) {
        return ResponseEntity.ok(service.addUsage(membershipId, dto));
    }

    @GetMapping
    public ResponseEntity<PageableResponse<MembershipUsageDTO>> list(
            @PathVariable Long membershipId,
            @PageableDefault Pageable pageable) {
        Page<MembershipUsageDTO> page = service.list(membershipId, pageable);
        return ResponseEntity.ok(new PageableResponse<>(page));
    }
}
