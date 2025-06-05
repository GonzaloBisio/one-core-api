package com.one.core.domain.repository.admin;

import com.one.core.domain.model.admin.SystemUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SystemUserRepository extends JpaRepository<SystemUser, Long> {
    Optional<SystemUser> findById(Long id);
    Optional<SystemUser> findByUsername(String username);
    boolean existsByUsername(String username);
}