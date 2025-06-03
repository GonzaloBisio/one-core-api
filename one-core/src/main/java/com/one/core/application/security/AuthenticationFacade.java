// src/main/java/com/one/core/application/security/AuthenticationFacade.java
package com.one.core.application.security;

import com.one.core.domain.model.admin.SystemUser;
import com.one.core.domain.repository.admin.SystemUserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class AuthenticationFacade {

    private static final Logger logger = LoggerFactory.getLogger(AuthenticationFacade.class);

    private final SystemUserRepository systemUserRepository;

    @Autowired
    public AuthenticationFacade(SystemUserRepository systemUserRepository) {
        this.systemUserRepository = systemUserRepository;
    }

    /**
     * Obtiene la entidad SystemUser completa del usuario actualmente autenticado.
     * Busca en la base de datos usando el ID o username del UserPrincipal.
     *
     * @return Un Optional<SystemUser> que estará vacío si no hay usuario autenticado,
     * o si el principal no es del tipo esperado, o si el usuario no se encuentra en la BD.
     */
    public Optional<SystemUser> getCurrentAuthenticatedSystemUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() ||
                authentication.getPrincipal() == null || "anonymousUser".equals(authentication.getPrincipal().toString())) {
            logger.debug("No authenticated user found in SecurityContext or user is anonymous.");
            return Optional.empty();
        }

        Object principal = authentication.getPrincipal();
        Long systemUserId = null;
        String username = null;

        if (principal instanceof UserPrincipal) {
            UserPrincipal userPrincipal = (UserPrincipal) principal;
            systemUserId = userPrincipal.getId(); // Asumiendo que UserPrincipal.getId() devuelve el SystemUser ID
            username = userPrincipal.getUsername(); // Como fallback o para logging
            logger.debug("Attempting to find SystemUser by ID: {} or Username: '{}' from UserPrincipal.", systemUserId, username);
        } else if (principal instanceof String) {
            username = (String) principal;
            logger.debug("Attempting to find SystemUser by Username: '{}' from String principal.", username);
        } else {
            logger.warn("Security principal is of an unexpected type: {}. Cannot extract user details.", principal.getClass().getName());
            return Optional.empty();
        }

        Optional<SystemUser> systemUserOptional = Optional.empty();
        if (systemUserId != null) {
            systemUserOptional = systemUserRepository.findById(systemUserId);
        }

        // Fallback a buscar por username si no se encontró por ID (o si systemUserId era null)
        if (systemUserOptional.isEmpty() && username != null) {
            logger.debug("SystemUser not found by ID (or ID was null), attempting findByUsername: '{}'", username);
            systemUserOptional = systemUserRepository.findByUsername(username);
        }

        if (systemUserOptional.isEmpty()) {
            logger.warn("SystemUser could not be loaded from repository for principal: {}", principal);
        }

        return systemUserOptional;
    }

    /**
     * Obtiene solo el ID del SystemUser actualmente autenticado desde el UserPrincipal.
     * Este método es más ligero si solo necesitas el ID y no toda la entidad.
     *
     * @return Un Optional<Long> con el ID del SystemUser, o vacío si no se puede determinar.
     */
    public Optional<Long> getCurrentAuthenticatedSystemUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() ||
                authentication.getPrincipal() == null || "anonymousUser".equals(authentication.getPrincipal().toString())) {
            logger.debug("No authenticated user ID found in SecurityContext or user is anonymous.");
            return Optional.empty();
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof UserPrincipal) {
            return Optional.ofNullable(((UserPrincipal) principal).getId());
        }
        // Si el principal es solo un String (username), no podemos obtener el ID directamente sin ir a la BD.
        // Para ese caso, se debería usar getCurrentAuthenticatedSystemUser().map(SystemUser::getId)
        logger.warn("Principal is not an instance of UserPrincipal. Cannot extract SystemUser ID directly. Principal type: {}", principal.getClass().getName());
        return Optional.empty();
    }
}