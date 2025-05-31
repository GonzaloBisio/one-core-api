package com.one.core.domain.repository.tenant.customer;

import com.one.core.domain.model.tenant.customer.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long>, JpaSpecificationExecutor<Customer> { // AÑADIR HERENCIA
    Optional<Customer> findByTaxId(String taxId);
    boolean existsByTaxId(String taxId);
    Optional<Customer> findByEmail(String email);
    boolean existsByEmail(String email);
}