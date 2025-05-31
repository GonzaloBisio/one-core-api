package com.one.core.application.dto.tenant.product;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductCategoryDTO {
    private Long id;

    @NotBlank(message = "Category name cannot be blank")
    @Size(max = 100, message = "Category name cannot exceed 100 characters")
    private String name;

    private String description;

    private Long parentId;        // ID de la categoría padre (opcional)
    private String parentName;    // Nombre de la categoría padre (solo para lectura/respuesta)

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}