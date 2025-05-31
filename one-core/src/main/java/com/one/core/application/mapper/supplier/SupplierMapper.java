package com.one.core.application.mapper.supplier;

import com.one.core.application.dto.tenant.supplier.SupplierDTO;
import com.one.core.domain.model.tenant.supplier.Supplier;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class SupplierMapper {

    public SupplierDTO toDTO(Supplier supplier) {
        if (supplier == null) {
            return null;
        }
        SupplierDTO dto = new SupplierDTO();
        dto.setId(supplier.getId());
        dto.setName(supplier.getName());
        dto.setContactPerson(supplier.getContactPerson());
        dto.setEmail(supplier.getEmail());
        dto.setPhone(supplier.getPhone());
        dto.setAddress(supplier.getAddress());
        dto.setTaxId(supplier.getTaxId());
        dto.setNotes(supplier.getNotes());
        dto.setCreatedAt(supplier.getCreatedAt());
        dto.setUpdatedAt(supplier.getUpdatedAt());
        return dto;
    }

    public void updateEntityFromDTO(SupplierDTO dto, Supplier entity) {
        entity.setName(dto.getName());
        entity.setContactPerson(StringUtils.hasText(dto.getContactPerson()) ? dto.getContactPerson().trim() : null);
        entity.setEmail(StringUtils.hasText(dto.getEmail()) ? dto.getEmail().trim() : null);
        entity.setPhone(StringUtils.hasText(dto.getPhone()) ? dto.getPhone().trim() : null);
        entity.setAddress(dto.getAddress()); // Asume que address puede ser más largo o null
        entity.setTaxId(StringUtils.hasText(dto.getTaxId()) ? dto.getTaxId().trim() : null);
        entity.setNotes(dto.getNotes());
        // createdAt y updatedAt son manejados por @PrePersist/@PreUpdate en la entidad
    }

    public Supplier toEntityForCreation(SupplierDTO dto) {
        Supplier entity = new Supplier();
        updateEntityFromDTO(dto, entity);
        return entity;
    }
}