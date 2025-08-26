package com.one.core.application.mapper.product;

import com.one.core.application.dto.tenant.product.ProductDTO;
import com.one.core.domain.model.enums.UnitOfMeasure;
import com.one.core.domain.model.tenant.product.Product;
import com.one.core.domain.service.common.UnitConversionService;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;

@Component
public class ProductMapper {

    private final UnitConversionService unitConversionService;

    public ProductMapper(UnitConversionService unitConversionService) {
        this.unitConversionService = unitConversionService;
    }

    /* ===========================
       Helpers null-safe
       =========================== */

    private static BigDecimal zeroIfNull(BigDecimal v) {
        return v != null ? v : BigDecimal.ZERO;
    }

    private static UnitOfMeasure defaultUom(UnitOfMeasure u) {
        return u != null ? u : UnitOfMeasure.UNIT;
    }

    private static String trimToNull(String s) {
        return StringUtils.hasText(s) ? s.trim() : null;
    }

    /* ===========================
       Entity -> DTO
       =========================== */
    public ProductDTO toDTO(Product product) {
        if (product == null) return null;

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

        // Precios con default seguro
        dto.setPurchasePrice(zeroIfNull(product.getPurchasePrice()));
        dto.setSalePrice(zeroIfNull(product.getSalePrice()));

        // UoM y cantidades: siempre con defaults seguros
        UnitOfMeasure uom = defaultUom(product.getUnitOfMeasure());
        BigDecimal current = zeroIfNull(product.getCurrentStock());
        BigDecimal minLvl = zeroIfNull(product.getMinimumStockLevel());

        UnitConversionService.NormalizedQuantity normalized =
                unitConversionService.normalizeFromBaseUnit(current, uom);

        dto.setUnitOfMeasure(normalized.unit());
        dto.setCurrentStock(normalized.quantity());
        dto.setMinimumStockLevel(
                unitConversionService.fromBaseUnit(minLvl, normalized.unit())
        );

        dto.setActive(product.isActive());
        dto.setBarcode(product.getBarcode());
        dto.setImageUrl(product.getImageUrl());

        return dto;
    }

    /* ===========================
       DTO -> Entity (update)
       =========================== */
    public void updateEntityFromDTO(ProductDTO dto, Product entity) {
        if (dto == null || entity == null) return;

        // String fields
        entity.setName(trimToNull(dto.getName()));
        entity.setSku(trimToNull(dto.getSku()));
        entity.setDescription(trimToNull(dto.getDescription()));
        entity.setBarcode(trimToNull(dto.getBarcode()));
        entity.setImageUrl(trimToNull(dto.getImageUrl()));

        // Solo pisamos productType si viene en el DTO
        if (dto.getProductType() != null) {
            entity.setProductType(dto.getProductType());
        }

        // Precios (si el DTO trae null, guardamos 0 para evitar NPEs en cálculos)
        entity.setSalePrice(zeroIfNull(dto.getSalePrice()));
        entity.setPurchasePrice(zeroIfNull(dto.getPurchasePrice()));

        // Cantidades: solo actualizamos si vienen informadas
        if (dto.getCurrentStock() != null) {
            entity.setCurrentStock(dto.getCurrentStock());
        }
        if (dto.getMinimumStockLevel() != null) {
            entity.setMinimumStockLevel(dto.getMinimumStockLevel());
        }

        // UoM: si viene en el DTO lo usamos; si no, garantizamos que el entity no quede en null
        if (dto.getUnitOfMeasure() != null) {
            entity.setUnitOfMeasure(dto.getUnitOfMeasure());
        } else if (entity.getUnitOfMeasure() == null) {
            entity.setUnitOfMeasure(UnitOfMeasure.UNIT);
        }

        // Active: si tu DTO usa boolean primitivo, esta línea está bien;
        // si fuera Boolean, conviene chequear null antes de pisar.
        entity.setActive(dto.isActive());
    }

    /* ===========================
       DTO -> Entity (create)
       =========================== */
    public Product toEntityForCreation(ProductDTO dto) {
        Product entity = new Product();
        updateEntityFromDTO(dto, entity);

        // Defaults finales de seguridad para evitar nulls en inserts
        if (entity.getUnitOfMeasure() == null) {
            entity.setUnitOfMeasure(UnitOfMeasure.UNIT);
        }
        if (entity.getCurrentStock() == null) {
            entity.setCurrentStock(BigDecimal.ZERO);
        }
        if (entity.getMinimumStockLevel() == null) {
            entity.setMinimumStockLevel(BigDecimal.ZERO);
        }
        if (entity.getPurchasePrice() == null) {
            entity.setPurchasePrice(BigDecimal.ZERO);
        }
        if (entity.getSalePrice() == null) {
            entity.setSalePrice(BigDecimal.ZERO);
        }

        return entity;
    }
}
