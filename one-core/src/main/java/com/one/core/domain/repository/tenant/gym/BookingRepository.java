package com.one.core.domain.repository.tenant.gym;

import com.one.core.domain.model.tenant.gym.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    boolean existsBySessionIdAndCustomerId(Long sessionId, Long customerId);
}