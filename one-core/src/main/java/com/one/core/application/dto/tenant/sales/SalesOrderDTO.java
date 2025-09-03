package com.one.core.application.dto.tenant.sales;

import com.one.core.domain.model.enums.sales.PaymentMethod;
import com.one.core.domain.model.enums.sales.SalesOrderStatus;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;

@Data
public class SalesOrderDTO {
    private Long id;
    private Long customerId;
    private String customerName;
    private LocalDate orderDate;
    private SalesOrderStatus status;
    private BigDecimal subtotalAmount;
    private BigDecimal taxAmount;
    private BigDecimal discountAmount;
    private BigDecimal totalAmount;
    private PaymentMethod paymentMethod;
    private String shippingAddress;
    private String notes;
    private Long createdByUserId;
    private String createdByUsername;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    private List<SalesOrderItemDTO> items;
}