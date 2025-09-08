package com.one.core.application.dto.tenant.gym;

import lombok.Data;

@Data
public class SubscriptionPlanDTO extends SubscriptionPlanCreateDTO {
    private Long id;
    private Long productId;
    private String productName;
    private Boolean active;
}
