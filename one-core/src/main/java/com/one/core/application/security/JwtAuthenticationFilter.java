package com.one.core.application.security;

import com.one.core.config.multitenancy.TenantContext;
import com.one.core.config.multitenancy.TenantInfo;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

    private final JwtTokenProvider tokenProvider;
    private final String jwtHeader;
    private final String jwtPrefix;

    public JwtAuthenticationFilter(JwtTokenProvider tokenProvider,
                                   @Value("${jwt.header}") String jwtHeader,
                                   @Value("${jwt.prefix}") String jwtPrefix) {
        this.tokenProvider = tokenProvider;
        this.jwtHeader = jwtHeader;
        this.jwtPrefix = jwtPrefix;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        try {
            String jwt = resolveToken(request);

            if (StringUtils.hasText(jwt) && tokenProvider.validateToken(jwt)) {
                // --- claims principales ---
                Long userId          = tokenProvider.getUserIdFromJWT(jwt);
                String username      = tokenProvider.getUsernameFromJWT(jwt);
                String tenantSchema  = tokenProvider.getTenantSchemaFromJWT(jwt);
                String industryType  = tokenProvider.getIndustryTypeFromJWT(jwt);
                Long tenantDbId      = tokenProvider.getTenantDbIdFromJWT(jwt);
                String tenantName    = tokenProvider.getTenantCompanyNameFromJWT(jwt);

                // --- multi-tenant context ---
                TenantContext.setCurrentTenant(new TenantInfo(tenantSchema, industryType));

                // --- authorities desde claim "roles" ---
                List<String> roles = tokenProvider.getRolesFromJWT(jwt);
                List<GrantedAuthority> authorities = (roles == null ? List.<String>of() : roles).stream()
                        .filter(StringUtils::hasText)
                        .map(r -> r.startsWith("ROLE_") ? r : "ROLE_" + r) // normalizar prefijo
                        .distinct()
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList());

                UserPrincipal principal = new UserPrincipal(
                        userId,
                        username,
                        null,
                        tenantSchema,
                        tenantDbId,
                        tenantName,
                        industryType,
                        authorities
                );

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(principal, null, authorities);
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (Exception ex) {
            logger.error("Could not set user authentication in security context", ex);
            SecurityContextHolder.clearContext();
        }

        try {
            filterChain.doFilter(request, response);
        } finally {
            // limpiar SIEMPRE el tenant del hilo
            TenantContext.clear();
        }
    }

    private String resolveToken(HttpServletRequest request) {
        String headerValue = request.getHeader(jwtHeader);
        if (!StringUtils.hasText(headerValue)) {
            return null;
        }
        String expectedPrefix = jwtPrefix + " ";
        if (headerValue.startsWith(expectedPrefix)) {
            return headerValue.substring(expectedPrefix.length());
        }
        // fallback: si viene el token "pelado" sin prefijo
        if (!headerValue.contains(" ")) {
            return headerValue;
        }
        return null;
    }
}
