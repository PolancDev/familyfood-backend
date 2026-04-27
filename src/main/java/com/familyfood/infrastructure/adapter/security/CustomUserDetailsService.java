package com.familyfood.infrastructure.adapter.security;

import com.familyfood.application.mapper.UserMapper;
import com.familyfood.domain.model.User;
import com.familyfood.infrastructure.adapter.persistence.repository.SpringDataUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Implementación de UserDetailsService de Spring Security que carga usuarios desde la base de datos.
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final SpringDataUserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .map(userMapper::toDomain)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado: " + email));

        return CustomUserDetails.fromDomain(user);
    }
}