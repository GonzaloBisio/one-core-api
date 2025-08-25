package com.one.core.application.mapper.product;

import com.one.core.application.dto.tenant.product.ProductDTO;
import com.one.core.domain.model.enums.UnitOfMeasure;
import com.one.core.domain.model.tenant.product.Product;
import org.springframework.stereotype.Component;
import com.one.core.domain.service.common.UnitConversionService;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;

@Component
public class ProductMapper {

    private final UnitConversionService unitConversionService;

    public ProductMapper(UnitConversionService unitConversionService) {
        this.unitConversionService = unitConversionService;
    }


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
        dto.setHasPackaging(product.isHasPackaging());

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

        UnitConversionService.NormalizedQuantity normalizedStock =
                unitConversionService.normalizeFromBaseUnit(product.getCurrentStock(), product.getUnitOfMeasure());
        dto.setUnitOfMeasure(normalizedStock.unit());
        dto.setCurrentStock(normalizedStock.quantity());
        dto.setMinimumStockLevel(unitConversionService.fromBaseUnit(product.getMinimumStockLevel(), normalizedStock.unit()));
        dto.setActive(product.isActive());
        dto.setBarcode(product.getBarcode());
        dto.setImageUrl(product.getImageUrl());

        return dto;
    }

    /**
     * Mapea los campos de un ProductDTO a una entidad Product existente.
     */
    public void updateEntityFromDTO(ProductDTO dto, Product entity) {
        entity.setName(dto.getName());
        entity.setProductType(dto.getProductType()); // <-- LÍNEA CLAVE AÑADIDA
        entity.setSku(StringUtils.hasText(dto.getSku()) ? dto.getSku().trim() : null);
        entity.setDescription(dto.getDescription());
        entity.setSalePrice(dto.getSalePrice() != null ? dto.getSalePrice() : BigDecimal.ZERO);
        entity.setPurchasePrice(dto.getPurchasePrice() != null ? dto.getPurchasePrice() : BigDecimal.ZERO);

        if (dto.getCurrentStock() != null) {
            entity.setCurrentStock(dto.getCurrentStock());
        }
        if (dto.getMinimumStockLevel() != null) {
            entity.setMinimumStockLevel(dto.getMinimumStockLevel());
        }

        entity.setUnitOfMeasure(dto.getUnitOfMeasure() != null ? dto.getUnitOfMeasure() : UnitOfMeasure.UNIT);
        entity.setActive(dto.isActive());
        entity.setBarcode(dto.getBarcode());
        entity.setImageUrl(dto.getImageUrl());
    }

    /**
     * Crea una nueva entidad Product a partir de un ProductDTO.
     */
    public Product toEntityForCreation(ProductDTO dto) {
        Product entity = new Product();
        // Al llamar a updateEntityFromDTO, ahora sí se copiará el productType
        updateEntityFromDTO(dto, entity);
        return entity;
    }
}