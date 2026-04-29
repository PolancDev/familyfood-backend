package com.familyfood.infrastructure.config;

import com.familyfood.application.port.repository.PasswordEncoder;
import com.familyfood.application.port.repository.UserRepository;
import com.familyfood.domain.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Inicializador de datos demo para la aplicación.
 * Solo inserta datos si la tabla de usuarios está vacía,
 * garantizando que sea idempotente y no falle en rearranques.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        // Verificar si la tabla tiene algún usuario (no solo admin)
        if (userRepository.count() > 0) {
            log.info("DataInitializer: ya existen usuarios en la BD. Omitiendo inicialización.");
            return;
        }

        log.info("DataInitializer: insertando datos demo...");

        try {
            // Usuario administrador demo
            User admin = User.create(
                    "admin@familyfood.com",
                    passwordEncoder.encode("admin123"),
                    "Admin FamilyFood"
            );
            userRepository.save(admin);
            log.info("DataInitializer: usuario admin creado (admin@familyfood.com)");

            // Usuario consumidor demo
            User consumer = User.create(
                    "consumer@familyfood.com",
                    passwordEncoder.encode("consumer123"),
                    "Consumer FamilyFood"
            );
            userRepository.save(consumer);
            log.info("DataInitializer: usuario consumer creado (consumer@familyfood.com)");

            log.info("DataInitializer: inicialización completada.");
        } catch (Exception e) {
            log.warn("DataInitializer: error al insertar datos demo (probablemente ya existen): {}", e.getMessage());
        }
    }
}
