// src/main/java/com/one/core/application/mapper/CustomerMapper.java
package com.one.core.application.mapper.customer;

import com.one.core.application.dto.tenant.customer.CustomerDTO;
import com.one.core.domain.model.tenant.customer.Customer;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class CustomerMapper {

    public CustomerDTO toDTO(Customer customer) {
        if (customer == null) {
            return null;
        }
        CustomerDTO dto = new CustomerDTO();
        dto.setId(customer.getId());
        dto.setName(customer.getName());
        dto.setCustomerType(customer.getCustomerType());
        dto.setTaxId(customer.getTaxId());
        dto.setEmail(customer.getEmail());
        dto.setPhone(customer.getPhone());
        dto.setAddress(customer.getAddress());
        dto.setCity(customer.getCity());
        dto.setPostalCode(customer.getPostalCode());
        dto.setCountry(customer.getCountry());
        dto.setActive(customer.isActive());
        dto.setCreatedAt(customer.getCreatedAt());
        dto.setUpdatedAt(customer.getUpdatedAt());
        return dto;
    }

    public void updateEntityFromDTO(CustomerDTO dto, Customer entity) {
        entity.setName(dto.getName());
        entity.setCustomerType(StringUtils.hasText(dto.getCustomerType()) ? dto.getCustomerType() : "INDIVIDUAL");
        entity.setTaxId(StringUtils.hasText(dto.getTaxId()) ? dto.getTaxId().trim() : null);
        entity.setEmail(StringUtils.hasText(dto.getEmail()) ? dto.getEmail().trim() : null);
        entity.setPhone(StringUtils.hasText(dto.getPhone()) ? dto.getPhone().trim() : null);
        entity.setAddress(dto.getAddress());
        entity.setCity(dto.getCity());
        entity.setPostalCode(dto.getPostalCode());
        entity.setCountry(dto.getCountry());
        entity.setActive(dto.isActive());
    }

    public Customer toEntityForCreation(CustomerDTO dto) {
        Customer entity = new Customer();
        updateEntityFromDTO(dto, entity);
        if (entity.getCustomerType() == null) { // Asegurar valor por defecto si updateEntityFromDTO no lo hizo
            entity.setCustomerType("INDIVIDUAL");
        }
        return entity;
    }
}