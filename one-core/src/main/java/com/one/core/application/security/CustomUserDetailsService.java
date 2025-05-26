package com.one.core.application.security;

import com.one.core.domain.model.admin.SystemUser;
import com.one.core.domain.model.admin.Tenant;
import com.one.core.domain.model.enums.SystemRole; // IMPORTA TU ENUM
import com.one.core.domain.repository.admin.SystemUserRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private SystemUserRepository systemUserRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        SystemUser systemUser = systemUserRepository.findByUsername(username)
                .orElseThrow(() ->
                        new UsernameNotFoundException("User not found with username: " + username));

        Tenant tenant = systemUser.getTenant();
        if (tenant == null) {
            throw new UsernameNotFoundException("User " + username + " is not associated with a valid tenant.");
        }

        String tenantSchema = tenant.getSchemaName(); // Nombre del esquema
        Long tenantDatabaseId = tenant.getId();        // ID de la BD del tenant
        String companyName = tenant.getCompanyName();  // Nombre de la compañía

        if (tenantSchema == null || tenantSchema.trim().isEmpty()) {
            throw new UsernameNotFoundException("Tenant schema name not found for user " + username);
        }
        if (tenantDatabaseId == null) {
            throw new UsernameNotFoundException("Tenant DB ID not found for user " + username);
        }
        if (companyName == null || companyName.trim().isEmpty()) {
            throw new UsernameNotFoundException("Tenant company name not found for user " + username);
        }

        List<GrantedAuthority> authorities = new ArrayList<>();
        SystemRole userSystemRole = systemUser.getSystemRole();

        if (userSystemRole == null) {
            // Esto no debería pasar si la columna system_role es NOT NULL
            throw new IllegalStateException("SystemUser " + username + " has no system role defined.");
        }

        // Asignar roles de Spring Security basados en el SystemRole del enum
        switch (userSystemRole) {
            case SUPER_ADMIN:
                authorities.add(new SimpleGrantedAuthority("ROLE_SUPER_ADMIN"));
                break;
            case TENANT_ADMIN:
                authorities.add(new SimpleGrantedAuthority("ROLE_TENANT_ADMIN"));
                break;
            case TENANT_USER:
                authorities.add(new SimpleGrantedAuthority("ROLE_TENANT_USER"));
                break;
            default:

                break;
        }


        return new UserPrincipal(
                systemUser.getId(),
                systemUser.getUsername(),
                systemUser.getPassword(),
                tenantSchema,
                tenantDatabaseId,
                companyName,
                authorities
        );
    }
}