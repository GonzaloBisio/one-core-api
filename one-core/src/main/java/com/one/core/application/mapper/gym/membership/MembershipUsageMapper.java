// src/main/java/com/one/core/application/mapper/gym/membership/MembershipUsageMapper.java
package com.one.core.application.mapper.gym.membership;

import com.one.core.application.dto.tenant.gym.MembershipUsageDTO;
import com.one.core.domain.model.tenant.gym.MembershipUsageEvent;
import org.springframework.stereotype.Component;

@Component
public class MembershipUsageMapper {

    public MembershipUsageDTO toDTO(MembershipUsageEvent e) {
        MembershipUsageDTO dto = new MembershipUsageDTO();
        dto.setId(e.getId());
        dto.setMembershipId(e.getMembership() != null ? e.getMembership().getId() : null);
        dto.setSessionId(e.getSession() != null ? e.getSession().getId() : null);
        dto.setEventDate(e.getEventDate());
        dto.setUnits(e.getUnits());
        dto.setReason(e.getReason());
        dto.setCreatedAt(e.getCreatedAt());
        return dto;
    }
}
