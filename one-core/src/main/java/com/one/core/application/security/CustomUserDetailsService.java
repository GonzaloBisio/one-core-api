package com.one.core.application.security;// package com.one.core.application.security;
// ... imports ...
import com.one.core.domain.model.admin.SystemUser;
import com.one.core.domain.model.admin.Tenant; // Asegúrate que usa tu Tenant actualizado
import com.one.core.domain.repository.admin.SystemUserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private SystemUserRepository systemUserRepository;

    @Override
    @Transactional()
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        SystemUser systemUser = systemUserRepository.findByUsername(username)
                .orElseThrow(() ->
                        new UsernameNotFoundException("User not found with username: " + username));

        Tenant tenant = systemUser.getTenant();
        if (tenant == null) {
            // Esto no debería pasar si tenant_id es NOT NULL y la FK está bien
            throw new UsernameNotFoundException("User " + username + " is not associated with a valid tenant.");
        }

        String tenantSchemaName = tenant.getSchemaName(); // <--- CAMBIO AQUÍ

        if (tenantSchemaName == null || tenantSchemaName.trim().isEmpty()) {
            throw new UsernameNotFoundException("Tenant schema name not found for user " + username);
        }

        // Roles: Placeholder por ahora. Implementaremos la carga real de roles del tenant más adelante.
        List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_TENANT_USER"));

        return new UserPrincipal(
                systemUser.getId(),
                systemUser.getUsername(),
                systemUser.getPassword(),
                tenantSchemaName, // Pasa el nombre del esquema aquí
                authorities
        );
    }
}