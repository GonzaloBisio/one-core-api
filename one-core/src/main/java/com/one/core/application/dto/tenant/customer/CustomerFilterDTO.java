
package com.one.core.application.dto.tenant.customer;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CustomerFilterDTO {
    private String name;        // Para buscar por nombre o razón social (parcial)
    private String email;       // Para buscar por email (parcial o exacto)
    private String taxId;       // Para buscar por CUIT/DNI exacto
    private String customerType; // Para filtrar por tipo de cliente (INDIVIDUAL, COMPANY)
    private Boolean isActive;   // Para filtrar por estado activo/inactivo
}