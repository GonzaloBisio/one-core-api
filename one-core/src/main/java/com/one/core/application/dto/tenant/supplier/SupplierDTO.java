package com.one.core.application.dto.tenant.supplier;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class SupplierDTO {
    private Long id;

    @NotBlank(message = "Supplier name cannot be blank")
    @Size(max = 150, message = "Supplier name cannot exceed 150 characters")
    private String name;

    @Size(max = 100, message = "Contact person name cannot exceed 100 characters")
    private String contactPerson;

    @Email(message = "Invalid email format")
    @Size(max = 100, message = "Email cannot exceed 100 characters")
    private String email;

    @Size(max = 50, message = "Phone number cannot exceed 50 characters")
    private String phone;

    private String address; // TEXT, no max size en DTO por ahora

    @Size(max = 50, message = "Tax ID cannot exceed 50 characters")
    private String taxId;

    private String notes; // TEXT

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}