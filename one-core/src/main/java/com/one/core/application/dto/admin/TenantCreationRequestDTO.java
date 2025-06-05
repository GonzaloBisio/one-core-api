package com.one.core.application.dto.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class TenantCreationRequestDTO {

    @NotBlank(message = "Company name is required")
    @Size(min = 3, max = 100, message = "Company name must be between 3 and 100 characters")
    private String companyName;

    @NotBlank(message = "Schema identifier is required")
    @Size(min = 3, max = 50, message = "Schema identifier must be between 3 and 50 characters")
    @Pattern(regexp = "^[a-z0-9_]+$", message = "Schema identifier can only contain lowercase letters, numbers, and underscores")
    private String schemaIdentifier; // ej: "ferreteria_la_tuerca"

    // --- Datos para el primer usuario (TENANT_ADMIN) de este tenant ---
    @NotBlank(message = "Admin username is required")
    @Size(min = 5, max = 100)
    private String adminUsername;

    @NotBlank(message = "Admin password is required")
    @Size(min = 8, message = "Admin password must be at least 8 characters long")
    private String adminPassword;

    @NotBlank(message = "Admin email is required")
    @Size(max = 100)
    private String adminEmail;

    @NotBlank(message = "Admin name is required")
    private String adminName;

    @NotBlank(message = "Admin last name is required")
    private String adminLastName;
}