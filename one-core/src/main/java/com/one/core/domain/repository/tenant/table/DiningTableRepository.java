package com.one.core.domain.repository.tenant.table;

import com.one.core.domain.model.tenant.table.DiningTable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DiningTableRepository extends JpaRepository<DiningTable, Long> {
}

