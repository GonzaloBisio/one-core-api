package com.one.core.domain.repository.tenant.table;

import com.one.core.domain.model.tenant.table.TableCheck;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TableCheckRepository extends JpaRepository<TableCheck, Long> {

    @Query(value = "SELECT * FROM table_checks tc WHERE tc.dining_table_id = :tableId AND tc.closed_at IS NULL", nativeQuery = true)
    Optional<TableCheck> findOpenByDiningTable(@Param("tableId") Long tableId);

    @Query(value = "SELECT * FROM table_checks tc WHERE tc.dining_table_id = :tableId AND tc.tenant_id = :tenantId AND tc.closed_at IS NULL", nativeQuery = true)
    Optional<TableCheck> findOpenByDiningTableAndTenant(@Param("tableId") Long tableId, @Param("tenantId") Long tenantId);
}

