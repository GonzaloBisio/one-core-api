package com.one.core.domain.service.tenant.supplier;

import com.one.core.application.dto.tenant.supplier.SupplierDTO;
import com.one.core.domain.model.tenant.supplier.Supplier;
import com.one.core.domain.repository.tenant.supplier.SupplierRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;


@Service
public class SupplierService {

    @Autowired
    private SupplierRepository supplierRepository;

    // Considera usar MapStruct para los mapeos
    private SupplierDTO convertToDTO(Supplier supplier) {
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

    private void mapDtoToEntity(SupplierDTO dto, Supplier entity) {
        entity.setName(dto.getName());
        entity.setContactPerson(dto.getContactPerson());
        entity.setEmail(dto.getEmail());
        entity.setPhone(dto.getPhone());
        entity.setAddress(dto.getAddress());
        entity.setTaxId(dto.getTaxId());
        entity.setNotes(dto.getNotes());
        // createdAt y updatedAt son manejados por @PrePersist/@PreUpdate o triggers
    }

    @Transactional(readOnly = true)
    public List<SupplierDTO> getAllSuppliers() {
        return supplierRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public SupplierDTO getSupplierById(Long id) {
        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Supplier not found with id: " + id)); // Reemplaza con ResourceNotFoundException
        return convertToDTO(supplier);
    }

    @Transactional
    public SupplierDTO createSupplier(SupplierDTO supplierDTO) {
        Supplier supplier = new Supplier();
        mapDtoToEntity(supplierDTO, supplier);
        Supplier savedSupplier = supplierRepository.save(supplier);
        return convertToDTO(savedSupplier);
    }

    @Transactional
    public SupplierDTO updateSupplier(Long id, SupplierDTO supplierDTO) {
        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Supplier not found with id: " + id)); // Reemplaza con ResourceNotFoundException
        mapDtoToEntity(supplierDTO, supplier);
        Supplier updatedSupplier = supplierRepository.save(supplier);
        return convertToDTO(updatedSupplier);
    }

    @Transactional
    public void deleteSupplier(Long id) {
        if (!supplierRepository.existsById(id)) {
            throw new RuntimeException("Supplier not found with id: " + id); // Reemplaza con ResourceNotFoundException
        }
        // Considera si hay lógica de negocio antes de borrar (ej. si tiene órdenes de compra activas)
        supplierRepository.deleteById(id);
    }
}
