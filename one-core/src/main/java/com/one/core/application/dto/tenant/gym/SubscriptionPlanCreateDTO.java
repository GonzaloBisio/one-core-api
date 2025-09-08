package com.one.core.application.dto.tenant.gym;


import com.one.core.domain.model.enums.gym.GymAccessMode;
import lombok.Data;

import java.util.List;

// --- Planes ---
@Data public class SubscriptionPlanCreateDTO {
    private String name;
    private String description;
    private GymAccessMode accessMode;
    private Integer visitsAllowed;        // null si UNLIMITED
    private Short resetDayOfWeek;         // 1..7 si N_PER_WEEK
    private Integer billingPeriodMonths;  // default 1
    private Long productId;               // Product vinculado (precio)
    private List<String> allowedClassTags;
    private Boolean active = true;
}