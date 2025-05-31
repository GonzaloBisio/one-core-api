package com.one.core.application.dto.tenant.supplier;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SupplierFilterDTO {
    private String name;
    private String taxId;
    private String email;
    private String phone;
    private String address;
}
