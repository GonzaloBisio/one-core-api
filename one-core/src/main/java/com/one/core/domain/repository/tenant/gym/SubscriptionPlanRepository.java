package com.one.core.domain.repository.tenant.gym;


import com.one.core.domain.model.tenant.gym.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface SubscriptionPlanRepository extends JpaRepository<SubscriptionPlan, Long> {
    boolean existsByNameIgnoreCase(String name);
    Optional<SubscriptionPlan> findByNameIgnoreCase(String name);
}