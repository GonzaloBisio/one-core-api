package com.one.core.domain.repository.tenant.table;

import com.one.core.domain.model.tenant.table.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
}

