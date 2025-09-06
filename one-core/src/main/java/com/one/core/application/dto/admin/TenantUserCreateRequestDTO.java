package com.one.core.application.dto.admin;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class TenantUserCreateRequestDTO {
    @NotBlank
    private String schemaName;
    @NotBlank private String username;
    @NotBlank private String password;
    @Email
    private String email;
    private String name;
    private String lastName;
    private Boolean activo = true;
}
