package com.familyfood.infrastructure.adapter.security;

import com.familyfood.domain.model.Role;
import com.familyfood.domain.model.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;
import java.util.UUID;

/**
 * Implementación de UserDetails de Spring Security que mapea desde el modelo de dominio User.
 */
@Data
@AllArgsConstructor
@Builder
public class CustomUserDetails implements UserDetails {

    private UUID id;
    private String email;
    private String password;
    private String nombre;
    private boolean enabled;
    private Role role;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (role == null) {
            return Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));
        }
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }

    @Override
    public String getUsername() {
        return email;
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
        return enabled;
    }

    /**
     * Devuelve el UUID del usuario.
     */
    public UUID getUserId() {
        return id;
    }

    public static CustomUserDetails fromDomain(User user) {
        return CustomUserDetails.builder()
                .id(user.getId())
                .email(user.getEmail())
                .password(user.getPassword())
                .nombre(user.getNombre())
                .enabled(true)
                .role(user.getRole())
                .build();
    }
}