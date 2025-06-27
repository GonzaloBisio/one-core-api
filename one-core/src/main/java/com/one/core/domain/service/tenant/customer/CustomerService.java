package com.one.core.domain.service.tenant.customer;

import com.one.core.application.dto.tenant.customer.CustomerDTO;
import com.one.core.application.dto.tenant.customer.CustomerFilterDTO;
import com.one.core.application.exception.DuplicateFieldException;
import com.one.core.application.exception.ResourceNotFoundException;
import com.one.core.application.mapper.customer.CustomerMapper;
import com.one.core.domain.model.tenant.customer.Customer;
import com.one.core.domain.repository.tenant.customer.CustomerRepository;
import com.one.core.domain.service.tenant.customer.criteria.CustomerSpecification;
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
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final CustomerMapper customerMapper;

    @Autowired
    public CustomerService(CustomerRepository customerRepository, CustomerMapper customerMapper) {
        this.customerRepository = customerRepository;
        this.customerMapper = customerMapper;
    }

    @Transactional(readOnly = true)
    public Page<CustomerDTO> getAllCustomers(CustomerFilterDTO filterDTO, Pageable pageable) {
        Specification<Customer> spec = CustomerSpecification.filterBy(filterDTO);
        Page<Customer> customerPage = customerRepository.findAll(spec, pageable);
        List<CustomerDTO> dtos = customerPage.getContent().stream().map(customerMapper::toDTO).collect(Collectors.toList());
        return new PageImpl<>(dtos, pageable, customerPage.getTotalElements());
    }

    @Transactional(readOnly = true)
    public CustomerDTO getCustomerById(Long id) {
        Customer customer = customerRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Customer", "id", id));
        return customerMapper.toDTO(customer);
    }

    @Transactional
    public CustomerDTO createCustomer(CustomerDTO customerDTO) {
        if (StringUtils.hasText(customerDTO.getTaxId()) && customerRepository.existsByTaxId(customerDTO.getTaxId().trim())) {
            throw new DuplicateFieldException("Customer Tax ID", customerDTO.getTaxId().trim());
        }
        Customer customer = customerMapper.toEntityForCreation(customerDTO);
        Customer savedCustomer = customerRepository.save(customer);
        return customerMapper.toDTO(savedCustomer);
    }

    @Transactional
    public CustomerDTO updateCustomer(Long id, CustomerDTO customerDTO) {
        Customer customer = customerRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Customer", "id", id));

        if (StringUtils.hasText(customerDTO.getTaxId())) {
            customerRepository.findByTaxId(customerDTO.getTaxId().trim()).ifPresent(existing -> {
                if (!existing.getId().equals(id)) throw new DuplicateFieldException("Customer Tax ID", customerDTO.getTaxId().trim());
            });
        }

        customerMapper.updateEntityFromDTO(customerDTO, customer);
        Customer updatedCustomer = customerRepository.save(customer);
        return customerMapper.toDTO(updatedCustomer);
    }

    @Transactional
    public void deleteCustomer(Long id) {
        if (!customerRepository.existsById(id)) throw new ResourceNotFoundException("Customer", "id", id);
        customerRepository.deleteById(id);
    }
}