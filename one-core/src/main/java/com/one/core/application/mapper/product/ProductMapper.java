package com.one.core.application.mapper.product;

import com.one.core.application.dto.tenant.product.ProductDTO;
import com.one.core.domain.model.tenant.product.Product;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;

@Component
public class ProductMapper {

    /**
     * Convierte una entidad Product a ProductDTO.
     */
    public ProductDTO toDTO(Product product) {
        if (product == null) {
            return null;
        }
        ProductDTO dto = new ProductDTO();
        dto.setId(product.getId());
        dto.setSku(product.getSku());
        dto.setName(product.getName());
        dto.setDescription(product.getDescription());
        dto.setProductType(product.getProductType());

        if (product.getCategory() != null) {
            dto.setCategoryId(product.getCategory().getId());
            dto.setCategoryName(product.getCategory().getName());
        }

        if (product.getDefaultSupplier() != null) {
            dto.setDefaultSupplierId(product.getDefaultSupplier().getId());
            dto.setDefaultSupplierName(product.getDefaultSupplier().getName());
        }

        dto.setPurchasePrice(product.getPurchasePrice());
        dto.setSalePrice(product.getSalePrice());
        dto.setUnitOfMeasure(product.getUnitOfMeasure());
        dto.setCurrentStock(product.getCurrentStock());
        dto.setMinimumStockLevel(product.getMinimumStockLevel());
        dto.setActive(product.isActive());
        dto.setBarcode(product.getBarcode());
        dto.setImageUrl(product.getImageUrl());
        // Podrías incluir createdAt y updatedAt si los tienes en el DTO
        // dto.setCreatedAt(product.getCreatedAt());
        // dto.setUpdatedAt(product.getUpdatedAt());
        return dto;
    }

    /**
     * Mapea los campos de un ProductDTO a una entidad Product existente.
     * No maneja la carga de ProductCategory o Supplier, eso se hace en el servicio.
     *
     * @param dto    El ProductDTO con los datos a mapear.
     * @param entity La entidad Product a actualizar.
     */
    public void updateEntityFromDTO(ProductDTO dto, Product entity) {
        entity.setName(dto.getName());
        entity.setSku(StringUtils.hasText(dto.getSku()) ? dto.getSku().trim() : null);
        entity.setDescription(dto.getDescription());
        entity.setSalePrice(dto.getSalePrice() != null ? dto.getSalePrice() : BigDecimal.ZERO);
        entity.setPurchasePrice(dto.getPurchasePrice() != null ? dto.getPurchasePrice() : BigDecimal.ZERO);

        // Para el stock, considera si la actualización directa es permitida o si debe ir vía StockMovements.
        // Por ahora, lo permitimos para una actualización general del producto.
        if (dto.getCurrentStock() != null) {
            entity.setCurrentStock(dto.getCurrentStock());
        }
        if (dto.getMinimumStockLevel() != null) {
            entity.setMinimumStockLevel(dto.getMinimumStockLevel());
        }

        entity.setUnitOfMeasure(StringUtils.hasText(dto.getUnitOfMeasure()) ? dto.getUnitOfMeasure() : "UNIT");
        entity.setActive(dto.isActive());
        entity.setBarcode(dto.getBarcode());
        entity.setImageUrl(dto.getImageUrl());

        // La asignación de category y defaultSupplier se hace en el servicio
        // después de cargar esas entidades por ID.
    }

    /**
     * Crea una nueva entidad Product a partir de un ProductDTO.
     * No maneja la carga de ProductCategory o Supplier.
     */
    public Product toEntityForCreation(ProductDTO dto) {
        Product entity = new Product();
        updateEntityFromDTO(dto, entity);

        // Campos que se setean solo en la creación y no se actualizan normalmente (o se manejan diferente)
        // El currentStock y minimumStockLevel ya se manejan en updateEntityFromDTO con valores por defecto
        // si el DTO los trae nulos, lo cual está bien para la creación.
        // Si el SKU es generado y no viene del DTO, se manejaría en el servicio.
        return entity;
    }
}