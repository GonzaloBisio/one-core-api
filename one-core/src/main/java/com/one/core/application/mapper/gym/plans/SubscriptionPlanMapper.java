package com.one.core.application.mapper.gym.plans;

import com.one.core.application.dto.tenant.gym.SubscriptionPlanCreateDTO;
import com.one.core.application.dto.tenant.gym.SubscriptionPlanDTO;
import com.one.core.domain.model.tenant.gym.SubscriptionPlan;
import com.one.core.domain.model.tenant.product.Product;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
public class SubscriptionPlanMapper {

    public SubscriptionPlan toNewEntity(SubscriptionPlanCreateDTO dto) {
        SubscriptionPlan p = new SubscriptionPlan();
        p.setName(dto.getName());
        p.setDescription(dto.getDescription());
        p.setAccessMode(dto.getAccessMode());
        p.setVisitsAllowed(dto.getVisitsAllowed());
        p.setResetDayOfWeek(dto.getResetDayOfWeek());
        p.setBillingPeriodMonths(dto.getBillingPeriodMonths() != null ? dto.getBillingPeriodMonths() : 1);
        if (dto.getAllowedClassTags() != null) {
            p.setAllowedClassTags(dto.getAllowedClassTags().toArray(String[]::new));
        }
        p.setActive(dto.getActive() == null || dto.getActive());
        return p;
    }

    public void applyProduct(SubscriptionPlan plan, Product product) {
        plan.setProduct(product);
    }

    public void applyUpdates(SubscriptionPlan plan, SubscriptionPlanCreateDTO dto) {
        if (dto.getName() != null) plan.setName(dto.getName());
        if (dto.getDescription() != null) plan.setDescription(dto.getDescription());
        if (dto.getAccessMode() != null) plan.setAccessMode(dto.getAccessMode());
        if (dto.getVisitsAllowed() != null) plan.setVisitsAllowed(dto.getVisitsAllowed());
        if (dto.getResetDayOfWeek() != null) plan.setResetDayOfWeek(dto.getResetDayOfWeek());
        if (dto.getBillingPeriodMonths() != null) plan.setBillingPeriodMonths(dto.getBillingPeriodMonths());
        if (dto.getActive() != null) plan.setActive(dto.getActive());
        if (dto.getAllowedClassTags() != null) {
            plan.setAllowedClassTags(dto.getAllowedClassTags().toArray(String[]::new));
        }
    }

    public SubscriptionPlanDTO toDTO(SubscriptionPlan p) {
        SubscriptionPlanDTO dto = new SubscriptionPlanDTO();
        dto.setId(p.getId());
        dto.setName(p.getName());
        dto.setDescription(p.getDescription());
        dto.setAccessMode(p.getAccessMode());
        dto.setVisitsAllowed(p.getVisitsAllowed());
        dto.setResetDayOfWeek(p.getResetDayOfWeek());
        dto.setBillingPeriodMonths(p.getBillingPeriodMonths());
        if (p.getAllowedClassTags() != null) {
            dto.setAllowedClassTags(Arrays.asList(p.getAllowedClassTags()));
        }
        dto.setActive(p.isActive());

        if (p.getProduct() != null) {
            dto.setProductId(p.getProduct().getId());
            dto.setProductName(p.getProduct().getName()); // <-- no pisar name del plan
        }
        return dto;
    }
}
