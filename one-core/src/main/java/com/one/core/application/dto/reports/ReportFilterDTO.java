package com.one.core.application.dto.reports;

import lombok.Data;

@Data
public class ReportFilterDTO {
    private String transactionType;
    private String paymentMethod;
}