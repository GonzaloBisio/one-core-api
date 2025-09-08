package com.one.core.application.dto.tenant.gym;
import com.one.core.domain.model.enums.sales.PaymentMethod;
import lombok.Data;
import java.time.LocalDate;


// --- Membresías ---
@Data public class MembershipCreateDTO {
    private Long customerId;
    private Long planId;
    private LocalDate startDate;               // hoy si null
    private Boolean autopay = false;
    private PaymentMethod preferredPaymentMethod;
    private String notes;
    private LocalDate endDate;                 // opcional, null = indefinido
}