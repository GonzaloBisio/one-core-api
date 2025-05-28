package com.one.core.domain.service.tenant.customer;

import com.one.core.application.dto.tenant.customer.CustomerDTO;
import com.one.core.application.exception.DuplicateFieldException;
import com.one.core.application.exception.ResourceNotFoundException;
import com.one.core.domain.model.tenant.customer.Customer;
import com.one.core.domain.repository.tenant.customer.CustomerRepository;
import org.flywaydb.core.internal.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CustomerService {

    @Autowired
    private CustomerRepository customerRepository;

    // Considera usar MapStruct para los mapeos
    private CustomerDTO convertToDTO(Customer customer) {
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

    private void mapDtoToEntity(CustomerDTO dto, Customer entity) {
        entity.setName(dto.getName());
        entity.setCustomerType(dto.getCustomerType() != null ? dto.getCustomerType() : "INDIVIDUAL");
        entity.setTaxId(dto.getTaxId());
        entity.setEmail(dto.getEmail());
        entity.setPhone(dto.getPhone());
        entity.setAddress(dto.getAddress());
        entity.setCity(dto.getCity());
        entity.setPostalCode(dto.getPostalCode());
        entity.setCountry(dto.getCountry());
        entity.setActive(dto.isActive());
        // createdAt y updatedAt son manejados por @PrePersist/@PreUpdate o triggers
    }

    @Transactional(readOnly = true)
    public List<CustomerDTO> getAllCustomers() {
        return customerRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CustomerDTO getCustomerById(Long id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Customer not found with id: " + id)); // Reemplaza con ResourceNotFoundException
        return convertToDTO(customer);
    }

    @Transactional
    public CustomerDTO createCustomer(CustomerDTO customerDTO) {

        String taxId = customerDTO.getTaxId();
        if (StringUtils.hasText(taxId)) {
            if (customerRepository.existsByTaxId(taxId.trim())) {
                throw new DuplicateFieldException("Tax ID", taxId.trim());
            }
            customerDTO.setTaxId(taxId.trim()); // Normalizar
        } else {
            customerDTO.setTaxId(null);
        }


        String email = customerDTO.getEmail();
        if (StringUtils.hasText(email)) {

            customerDTO.setEmail(email.trim());
        }


        Customer customer = new Customer();
        mapDtoToEntity(customerDTO, customer);
        if (customer.getCustomerType() == null) {
            customer.setCustomerType("INDIVIDUAL");
        }
        Customer savedCustomer = customerRepository.save(customer);
        return convertToDTO(savedCustomer);
    }

    @Transactional
    public CustomerDTO updateCustomer(Long id, CustomerDTO customerDTO) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "id", id));

        // Validar Tax ID (si se provee y cambió)
        String taxId = customerDTO.getTaxId();
        if (StringUtils.hasText(taxId)) {
            Optional<Customer> existingCustomerByTaxId = customerRepository.findByTaxId(taxId.trim());
            if (existingCustomerByTaxId.isPresent() && !existingCustomerByTaxId.get().getId().equals(id)) {
                throw new DuplicateFieldException("Tax ID", taxId.trim());
            }
            customerDTO.setTaxId(taxId.trim());
        } else {
            customerDTO.setTaxId(null); // Permitir quitarlo si es opcional
        }

        // Validar Email (si se provee y cambió y quieres que sea único)
        String email = customerDTO.getEmail();
        if (StringUtils.hasText(email)) {
            // Optional<Customer> existingCustomerByEmail = customerRepository.findByEmail(email.trim());
            // if (existingCustomerByEmail.isPresent() && !existingCustomerByEmail.get().getId().equals(id)) {
            //     throw new DuplicateFieldException("Email", email.trim());
            // }
            customerDTO.setEmail(email.trim());
        }

        mapDtoToEntity(customerDTO, customer);
        Customer updatedCustomer = customerRepository.save(customer);
        return convertToDTO(updatedCustomer);
    }

    @Transactional
    public void deleteCustomer(Long id) {
        if (!customerRepository.existsById(id)) {
            throw new RuntimeException("Customer not found with id: " + id); // Reemplaza con ResourceNotFoundException
        }
        // Considera lógica de negocio (ej. si tiene pedidos pendientes)
        customerRepository.deleteById(id);
    }
}