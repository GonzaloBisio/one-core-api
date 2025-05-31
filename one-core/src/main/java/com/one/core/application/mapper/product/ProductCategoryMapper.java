package com.one.core.application.mapper.product;

import com.one.core.application.dto.tenant.product.ProductCategoryDTO;
import com.one.core.domain.model.tenant.product.ProductCategory;
import org.springframework.stereotype.Component;

@Component
public class ProductCategoryMapper {

    public ProductCategoryDTO toDTO(ProductCategory entity) {
        if (entity == null) {
            return null;
        }
        ProductCategoryDTO dto = new ProductCategoryDTO();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setDescription(entity.getDescription());
        if (entity.getParentCategory() != null) {
            dto.setParentId(entity.getParentCategory().getId());
            dto.setParentName(entity.getParentCategory().getName());
        }
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        return dto;
    }

    /**
     * Actualiza los campos básicos de una entidad desde un DTO.
     * NO establece parentCategory aquí; eso se hace en el servicio.
     */
    public void updateEntityFromDTO(ProductCategoryDTO dto, ProductCategory entity) {
        entity.setName(dto.getName());
        entity.setDescription(dto.getDescription());
        // El parentCategory se maneja en el servicio
    }

    public ProductCategory toEntityForCreation(ProductCategoryDTO dto) {
        ProductCategory entity = new ProductCategory();
        updateEntityFromDTO(dto, entity);
        // parentCategory se establece en el servicio
        return entity;
    }
}