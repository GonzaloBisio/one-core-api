package com.one.core.application.mapper.inventory;

import com.one.core.application.dto.tenant.inventory.StockMovementDTO;
import com.one.core.domain.model.admin.SystemUser; // IMPORTANTE: Usar SystemUser
import com.one.core.domain.model.tenant.product.StockMovement;
import com.one.core.domain.model.tenant.product.Product;
import org.springframework.stereotype.Component;

@Component
public class StockMovementMapper {

    public StockMovementDTO toDTO(StockMovement entity) {
        if (entity == null) {
            return null;
        }

        StockMovementDTO dto = new StockMovementDTO();
        dto.setId(entity.getId());

        Product product = entity.getProduct();
        if (product != null) {
            dto.setProductId(product.getId());
            dto.setProductName(product.getName());
        }

        dto.setMovementType(entity.getMovementType());
        dto.setQuantityChanged(entity.getQuantityChanged());
        dto.setStockAfterMovement(entity.getStockAfterMovement());
        dto.setMovementDate(entity.getMovementDate());
        dto.setReferenceDocumentType(entity.getReferenceDocumentType());
        dto.setReferenceDocumentId(entity.getReferenceDocumentId());

        // Obtener información del SystemUser
        SystemUser user = entity.getUser(); // Ahora 'user' es de tipo SystemUser
        if (user != null) {
            dto.setUserId(user.getId());         // ID del SystemUser
            dto.setUsername(user.getUsername()); // Username del SystemUser
            // Si quisieras el nombre completo del SystemUser y lo tienes en esa entidad:
            // dto.setFullName(user.getName() + " " + user.getLastName());
        }
        dto.setNotes(entity.getNotes());

        return dto;
    }

    // Si necesitas un método para convertir DTO a Entidad (toEntity),
    // recuerda que el campo 'user' en StockMovement ahora esperaría un SystemUser.
    // Generalmente, el servicio construiría la entidad StockMovement y asignaría el SystemUser
    // obtenido a través del AuthenticationFacade.
}