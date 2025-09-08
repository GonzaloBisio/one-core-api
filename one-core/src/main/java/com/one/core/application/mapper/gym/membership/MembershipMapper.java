package com.one.core.application.mapper.gym.membership;

import com.one.core.application.dto.tenant.gym.MembershipCreateDTO;
import com.one.core.application.dto.tenant.gym.MembershipDTO;
import com.one.core.domain.model.enums.gym.MembershipStatus;
import com.one.core.domain.model.tenant.customer.Customer;
import com.one.core.domain.model.tenant.gym.Membership;
import com.one.core.domain.model.tenant.gym.SubscriptionPlan;
import org.springframework.stereotype.Component;

@Component
public class MembershipMapper {

    /** Crea entidad nueva (sin asociaciones). El service setea plan y customer. */
    public Membership toNewEntity(MembershipCreateDTO dto) {
        Membership m = new Membership();
        m.setStatus(MembershipStatus.ACTIVE);                 // default
        m.setStartDate(dto.getStartDate());
        m.setEndDate(dto.getEndDate());
        m.setAutopay(Boolean.TRUE.equals(dto.getAutopay()));
        m.setPreferredPaymentMethod(dto.getPreferredPaymentMethod());
        m.setNotes(dto.getNotes());
        // nextBillingDate se calcula en el service según el plan
        return m;
    }

    /** Aplica asociaciones resueltas por el service. */
    public void applyAssociations(Membership target, Customer customer, SubscriptionPlan plan) {
        target.setCustomer(customer);
        target.setPlan(plan);
    }

    /** Entidad -> DTO (alineado a MembershipDTO actual). */
    public MembershipDTO toDTO(Membership m) {
        MembershipDTO dto = new MembershipDTO();
        dto.setId(m.getId());

        dto.setCustomerId(m.getCustomer() != null ? m.getCustomer().getId() : null);
        dto.setPlanId(m.getPlan() != null ? m.getPlan().getId() : null);

        dto.setStatus(m.getStatus() != null ? m.getStatus().name() : null);
        dto.setStartDate(m.getStartDate());
        dto.setEndDate(m.getEndDate());
        dto.setNextBillingDate(m.getNextBillingDate());

        dto.setAutopay(m.isAutopay());
        dto.setPreferredPaymentMethod(m.getPreferredPaymentMethod());
        dto.setNotes(m.getNotes());
        return dto;
    }

    /** (Opcional) Actualización parcial desde un DTO de update si en el futuro lo agregás. */
    public void copyUpdatableFields(MembershipDTO dto, Membership target) {
        // ejemplo: si en el futuro permitís pausar/cancelar desde un DTO
        if (dto.getStatus() != null) {
            try {
                target.setStatus(MembershipStatus.valueOf(dto.getStatus()));
            } catch (IllegalArgumentException ignored) {
                // status inválido -> lo ignoramos o lanzamos ValidationException en el service
            }
        }
        if (dto.getEndDate() != null) target.setEndDate(dto.getEndDate());
        if (dto.getAutopay() != null) target.setAutopay(dto.getAutopay());
        if (dto.getPreferredPaymentMethod() != null) target.setPreferredPaymentMethod(dto.getPreferredPaymentMethod());
        if (dto.getNotes() != null) target.setNotes(dto.getNotes());
    }
}
