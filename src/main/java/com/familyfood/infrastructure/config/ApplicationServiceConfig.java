package com.familyfood.infrastructure.config;

//import com.familyfood.application.mapper.AuthMapper;
import com.familyfood.application.mapper.AuthResponseMapper;
import com.familyfood.application.port.repository.JwtService;
import com.familyfood.application.port.repository.PasswordEncoder;
import com.familyfood.application.port.repository.UserRepository;
import com.familyfood.application.service.AuthService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApplicationServiceConfig {

    @Bean
    public AuthService authService(UserRepository userRepository,
                                    PasswordEncoder passwordEncoder,
                                    JwtService jwtService,
                                   // AuthMapper authMapper,
                                    AuthResponseMapper authResponseMapper) {
        //return new AuthService(userRepository, passwordEncoder, jwtService, authMapper, authResponseMapper);
        return new AuthService(userRepository, passwordEncoder, jwtService, authResponseMapper);
    }
}