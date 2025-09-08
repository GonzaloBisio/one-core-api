package com.one.core.application.dto.tenant.gym;

import com.one.core.domain.model.enums.sales.PaymentMethod;
import lombok.Data;

import java.time.LocalDate;

@Data
public class MembershipDTO {
    private Long id;
    private Long customerId;
    private Long planId;
    private String status;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDate nextBillingDate;
    private Boolean autopay;
    private PaymentMethod preferredPaymentMethod;
    private String notes;
}