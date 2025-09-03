package com.one.core.application.dto.reports;

import com.one.core.domain.model.enums.sales.PaymentMethod;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class ReportFilterDTO {
    private String transactionType;

    @Schema(description = "Método de pago. Valores permitidos: DEBIT, CREDIT, TRANSFER, CASH")
    private PaymentMethod paymentMethod;
}