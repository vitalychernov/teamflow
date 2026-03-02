package com.teamflow.security;

import com.teamflow.entity.Role;
import com.teamflow.entity.User;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * Adapter between our User entity and Spring Security's UserDetails interface.
 *
 * Spring Security knows nothing about our User entity — it works with UserDetails.
 * This class bridges them. It wraps User and exposes what Spring Security needs.
 *
 * Why not make User implement UserDetails directly?
 * That would couple the domain entity to Spring Security — a framework concern.
 * Keeping them separate allows changing either without affecting the other.
 */
@Getter
public class CustomUserDetails implements UserDetails {

    private final User user;

    public CustomUserDetails(User user) {
        this.user = user;
    }

    // Convenience methods used in service layer
    public Long getId() {
        return user.getId();
    }

    public boolean isAdmin() {
        return user.getRole() == Role.ADMIN;
    }

    /**
     * Maps our Role enum to Spring Security's GrantedAuthority.
     * Spring's hasRole("ADMIN") checks for authority "ROLE_ADMIN" —
     * the "ROLE_" prefix is added automatically by SimpleGrantedAuthority
     * when used with hasRole(), but we add it explicitly here for clarity.
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));
    }

    @Override
    public String getPassword() {
        return user.getPassword();
    }

    // Spring Security uses email as the username identifier
    @Override
    public String getUsername() {
        return user.getEmail();
    }

    // Simplified: all accounts are always active and non-expired
    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled() { return true; }
}
