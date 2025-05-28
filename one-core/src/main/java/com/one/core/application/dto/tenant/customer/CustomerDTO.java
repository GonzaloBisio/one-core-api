package com.one.core.application.dto.tenant.customer;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CustomerDTO {
    private Long id;

    @NotBlank(message = "Customer name cannot be blank")
    @Size(max = 150, message = "Customer name cannot exceed 150 characters")
    private String name; // Nombre o Razón Social

    @Size(max = 20)
    private String customerType; // INDIVIDUAL, COMPANY

    @NotBlank(message = "Tax ID cannot be null or empty if provided, or make it truly optional")
    @Size(max = 50, message = "Tax ID cannot exceed 50 characters")
    private String taxId;

    @Email(message = "Invalid email format")
    @Size(max = 100, message = "Email cannot exceed 100 characters")
    private String email;

    @Size(max = 50, message = "Phone number cannot exceed 50 characters")
    private String phone;

    private String address;

    @Size(max = 100)
    private String city;

    @Size(max = 20)
    private String postalCode;

    @Size(max = 50)
    private String country;

    private boolean isActive = true;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}