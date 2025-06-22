package com.one.core.config;

import com.one.core.domain.model.admin.SystemUser;
import com.one.core.domain.repository.admin.SystemUserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
public class JpaConfig {

    @Autowired
    private SystemUserRepository systemUserRepository;

    @Bean
    public AuditorAware<SystemUser> auditorProvider() {
        return () -> {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication == null || !authentication.isAuthenticated()) {
                return Optional.empty();
            }

            Object principal = authentication.getPrincipal();

            String username = null;

            if (principal instanceof String) {
                username = (String) principal;
            }

            if (username == null) {
                return Optional.empty();
            }

            return systemUserRepository.findByUsername(username);
        };
    }
}