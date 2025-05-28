package com.one.core.domain.repository.tenant;

import com.one.core.domain.model.tenant.TenantUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface TenantUserRepository extends JpaRepository<TenantUser, Long> {
    Optional<TenantUser> findByUsername(String username);
    Optional<TenantUser> findByEmail(String email);
}