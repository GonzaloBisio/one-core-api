package com.one.core.application.dto.tenant.sales;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import com.one.core.domain.model.enums.sales.PaymentMethod;
import lombok.Data;
import java.util.List;

@Data
public class SalesOrderRequestDTO {
    private Long customerId;
    @NotEmpty private List<@Valid SalesOrderItemRequestDTO> items;
    private List<@Valid SalesOrderPackagingRequestDTO> packaging;
    private PaymentMethod paymentMethod;
    private String shippingAddress;
    private String notes;

}