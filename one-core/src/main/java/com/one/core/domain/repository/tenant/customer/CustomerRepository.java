package com.one.core.domain.repository.tenant.customer;

import com.one.core.domain.model.tenant.customer.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
    boolean existsByTaxId(String taxId);
    Optional<Customer> findByTaxId(String taxId); // Para la validación en update

}