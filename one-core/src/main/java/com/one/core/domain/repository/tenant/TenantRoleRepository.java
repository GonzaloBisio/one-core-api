package com.one.core.domain.repository.tenant;

import com.one.core.domain.model.tenant.TenantRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface TenantRoleRepository extends JpaRepository<TenantRole, Long> {
    Optional<TenantRole> findByRoleName(String roleName);
}