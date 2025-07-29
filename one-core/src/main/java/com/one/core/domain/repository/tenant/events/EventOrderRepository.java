package com.one.core.domain.repository.tenant.events;

import com.one.core.domain.model.tenant.events.EventOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface EventOrderRepository extends JpaRepository<EventOrder, Long>, JpaSpecificationExecutor<EventOrder> {
}