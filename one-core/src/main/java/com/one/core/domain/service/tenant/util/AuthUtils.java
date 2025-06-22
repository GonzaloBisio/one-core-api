package com.one.core.domain.service.tenant.util;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

public class AuthUtils {

    public static String getCurrentUsername() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication instanceof JwtAuthenticationToken jwtAuth) {
            Jwt token = jwtAuth.getToken();
            return token.getSubject();
        }

        return null;
    }

    public static String getCurrentTenantSchema() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication instanceof JwtAuthenticationToken jwtAuth) {
            Jwt token = jwtAuth.getToken();
            return token.getClaimAsString("tenantSchema");
        }

        return null;
    }
}

