package com.one.core.application.mapper.production;

import com.one.core.application.dto.tenant.production.ProductionOrderDTO;
import com.one.core.domain.model.tenant.production.ProductionOrder;
import org.springframework.stereotype.Component;

@Component
public class ProductionOrderMapper {

    public ProductionOrderDTO toDTO(ProductionOrder entity) {
        if (entity == null) {
            return null;
        }

        ProductionOrderDTO dto = new ProductionOrderDTO();
        dto.setId(entity.getId());
        dto.setProductionDate(entity.getProductionDate());
        dto.setNotes(entity.getNotes());
        dto.setQuantityProduced(entity.getQuantityProduced());
        dto.setCreatedAt(entity.getCreatedAt());

        if (entity.getProduct() != null) {
            dto.setProductId(entity.getProduct().getId());
            dto.setProductName(entity.getProduct().getName());
            dto.setProductSku(entity.getProduct().getSku());
        }

        if (entity.getCreatedByUser() != null) {
            dto.setCreatedByUserId(entity.getCreatedByUser().getId());
            dto.setCreatedByUsername(entity.getCreatedByUser().getUsername());
        }

        return dto;
    }
}