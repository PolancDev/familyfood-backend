package com.familyfood.infrastructure.config;

//import com.familyfood.application.mapper.AuthMapper;
import com.familyfood.application.mapper.AuthResponseMapper;
import com.familyfood.application.mapper.FamilyMapper;
import com.familyfood.application.mapper.RecipeMapper;
import com.familyfood.application.port.repository.FamilyGroupRepository;
import com.familyfood.application.port.repository.FamilyMemberRepository;
import com.familyfood.application.port.repository.JoinRequestRepository;
import com.familyfood.application.port.repository.JwtService;
import com.familyfood.application.port.repository.PasswordEncoder;
import com.familyfood.application.port.repository.RecipeRepository;
import com.familyfood.application.port.repository.UserRepository;
import com.familyfood.application.service.AuthService;
import com.familyfood.application.service.FamilyService;
import com.familyfood.application.service.RecipeService;

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

    @Bean
    public FamilyService familyService(FamilyGroupRepository familyGroupRepository,
                                       FamilyMemberRepository familyMemberRepository,
                                       JoinRequestRepository joinRequestRepository,
                                       UserRepository userRepository,
                                       FamilyMapper familyMapper) {
        return new FamilyService(familyGroupRepository, familyMemberRepository,
                joinRequestRepository, userRepository, familyMapper);
    }

    @Bean
    public RecipeService recipeService(RecipeRepository recipeRepository,
                                       RecipeMapper recipeMapper) {
        return new RecipeService(recipeRepository, recipeMapper);
    }
}