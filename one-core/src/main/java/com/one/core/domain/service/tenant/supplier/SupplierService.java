package com.one.core.domain.service.tenant.supplier;

import com.one.core.application.dto.tenant.supplier.SupplierDTO;
import com.one.core.application.dto.tenant.supplier.SupplierFilterDTO;
import com.one.core.application.exception.DuplicateFieldException;
import com.one.core.application.exception.ResourceNotFoundException;
import com.one.core.application.mapper.supplier.SupplierMapper;
import com.one.core.domain.model.tenant.supplier.Supplier;
import com.one.core.domain.repository.tenant.supplier.SupplierRepository;
import com.one.core.domain.service.tenant.supplier.criteria.SupplierSpecification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SupplierService {

    private final SupplierRepository supplierRepository;
    private final SupplierMapper supplierMapper;

    @Autowired
    public SupplierService(SupplierRepository supplierRepository, SupplierMapper supplierMapper) {
        this.supplierRepository = supplierRepository;
        this.supplierMapper = supplierMapper;
    }

    @Transactional(readOnly = true)
    public Page<SupplierDTO> getAllSuppliers(SupplierFilterDTO filterDTO, Pageable pageable) {
        Specification<Supplier> spec = SupplierSpecification.filterBy(filterDTO);
        Page<Supplier> supplierPage = supplierRepository.findAll(spec, pageable);
        List<SupplierDTO> supplierDTOs = supplierPage.getContent().stream()
                .map(supplierMapper::toDTO)
                .collect(Collectors.toList());
        return new PageImpl<>(supplierDTOs, pageable, supplierPage.getTotalElements());
    }

    @Transactional(readOnly = true)
    public SupplierDTO getSupplierById(Long id) {
        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier", "id", id));
        return supplierMapper.toDTO(supplier);
    }

    @Transactional
    public SupplierDTO createSupplier(SupplierDTO supplierDTO) {
        if (StringUtils.hasText(supplierDTO.getTaxId()) && supplierRepository.existsByTaxId(supplierDTO.getTaxId().trim())) {
            throw new DuplicateFieldException("Supplier Tax ID", supplierDTO.getTaxId().trim());
        }
        if (StringUtils.hasText(supplierDTO.getEmail()) && supplierRepository.existsByEmail(supplierDTO.getEmail().trim())) {
            throw new DuplicateFieldException("Supplier Email", supplierDTO.getEmail().trim());
        }

        Supplier supplier = supplierMapper.toEntityForCreation(supplierDTO);
        Supplier savedSupplier = supplierRepository.save(supplier);
        return supplierMapper.toDTO(savedSupplier);
    }

    @Transactional
    public SupplierDTO updateSupplier(Long id, SupplierDTO supplierDTO) {
        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Supplier", "id", id));

        if (StringUtils.hasText(supplierDTO.getTaxId()) && !supplierDTO.getTaxId().trim().equalsIgnoreCase(supplier.getTaxId())) {
            if (supplierRepository.existsByTaxId(supplierDTO.getTaxId().trim())) {
                throw new DuplicateFieldException("Supplier Tax ID", supplierDTO.getTaxId().trim());
            }
        }

        supplierMapper.updateEntityFromDTO(supplierDTO, supplier);
        Supplier updatedSupplier = supplierRepository.save(supplier);
        return supplierMapper.toDTO(updatedSupplier);
    }

    @Transactional
    public void deleteSupplier(Long id) {
        if (!supplierRepository.existsById(id)) {
            throw new ResourceNotFoundException("Supplier", "id", id);
        }
        supplierRepository.deleteById(id);
    }
}