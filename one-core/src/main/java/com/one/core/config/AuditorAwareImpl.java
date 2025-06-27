package com.one.core.config;

import com.one.core.application.security.UserPrincipal;
import com.one.core.domain.model.admin.SystemUser;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

public class AuditorAwareImpl implements AuditorAware<SystemUser> {

    @Override
    public Optional<SystemUser> getCurrentAuditor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() || !(authentication.getPrincipal() instanceof UserPrincipal)) {
            return Optional.empty();
        }

        UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();

        // Creamos una instancia proxy de SystemUser solo con el ID.
        // JPA es lo suficientemente inteligente como para manejar esto al persistir.
        SystemUser systemUserProxy = new SystemUser();
        systemUserProxy.setId(userPrincipal.getId());

        return Optional.of(systemUserProxy);
    }
}