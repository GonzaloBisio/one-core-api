package com.one.core.application.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Objects;

public class UserPrincipal implements UserDetails {
    private final Long id;
    private final String username;
    private final String password;
    private final String tenantSchemaName;
    private final Long tenantDbId;
    private final String tenantCompanyName;
    private final String industryType;
    private final Collection<? extends GrantedAuthority> authorities;

    public UserPrincipal(Long id, String username, String password,
                         String tenantSchemaName, Long tenantDbId, String tenantCompanyName, String industryType,
                         Collection<? extends GrantedAuthority> authorities) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.tenantSchemaName = tenantSchemaName;
        this.tenantDbId = tenantDbId;
        this.tenantCompanyName = tenantCompanyName;
        this.industryType = industryType;
        this.authorities = authorities;
    }

    public Long getId() {
        return id;
    }

    public String getTenantSchemaName() { // Renombrado de getTenantId() para claridad
        return tenantSchemaName;
    }

    public String getTenantCompanyName() { // NUEVO getter
        return tenantCompanyName;
    }


    public Long getTenantDbId() { // NUEVO getter
        return tenantDbId;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public String getPassword() {
        return password;
    }

    public String getIndustryType() {
        return industryType;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    // Es buena idea implementar equals y hashCode
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserPrincipal that = (UserPrincipal) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}