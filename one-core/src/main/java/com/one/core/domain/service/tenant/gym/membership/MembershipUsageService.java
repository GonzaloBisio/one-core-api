// src/main/java/com/one/core/domain/service/tenant/gym/membership/MembershipUsageService.java
package com.one.core.domain.service.tenant.gym.membership;

import com.one.core.application.dto.tenant.gym.MembershipUsageCreateDTO;
import com.one.core.application.dto.tenant.gym.MembershipUsageDTO;
import com.one.core.application.exception.ResourceNotFoundException;
import com.one.core.application.mapper.gym.membership.MembershipUsageMapper;
import com.one.core.domain.model.tenant.gym.ClassSession;
import com.one.core.domain.model.tenant.gym.Membership;
import com.one.core.domain.model.tenant.gym.MembershipUsageEvent;
import com.one.core.domain.repository.tenant.gym.ClassSessionRepository;
import com.one.core.domain.repository.tenant.gym.MembershipRepository;
import com.one.core.domain.repository.tenant.gym.MembershipUsageEventRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
public class MembershipUsageService {

    private final MembershipRepository membershipRepo;
    private final ClassSessionRepository sessionRepo;
    private final MembershipUsageEventRepository usageRepo;
    private final MembershipUsageMapper mapper;

    public MembershipUsageService(MembershipRepository membershipRepo,
                                  ClassSessionRepository sessionRepo,
                                  MembershipUsageEventRepository usageRepo,
                                  MembershipUsageMapper mapper) {
        this.membershipRepo = membershipRepo;
        this.sessionRepo = sessionRepo;
        this.usageRepo = usageRepo;
        this.mapper = mapper;
    }

    @Transactional
    public MembershipUsageDTO addUsage(Long membershipId, MembershipUsageCreateDTO dto) {
        Membership m = membershipRepo.findById(membershipId)
                .orElseThrow(() -> new ResourceNotFoundException("Membership","id", membershipId));

        ClassSession s = null;
        if (dto.getSessionId() != null) {
            s = sessionRepo.findById(dto.getSessionId())
                    .orElseThrow(() -> new ResourceNotFoundException("ClassSession","id", dto.getSessionId()));
        }

        MembershipUsageEvent e = new MembershipUsageEvent();
        e.setMembership(m);
        e.setSession(s);
        e.setUnits(dto.getUnits());
        e.setReason(dto.getReason());
        e.setEventDate(dto.getEventDate() != null ? dto.getEventDate() : LocalDate.now());

        return mapper.toDTO(usageRepo.save(e));
    }

    @Transactional(readOnly = true)
    public Page<MembershipUsageDTO> list(Long membershipId, Pageable pageable) {
        return usageRepo.findByMembershipIdOrderByEventDateDesc(membershipId, pageable)
                .map(mapper::toDTO);
    }
}
