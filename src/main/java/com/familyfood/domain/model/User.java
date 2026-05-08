package com.familyfood.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

import static com.familyfood.domain.model.Role.INVITADO;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    private UUID id;
    private String email;
    private String password;
    private String nombre;
    private LocalDateTime fechaCreacion;
    private LocalDateTime fechaActualizacion;
    private Preferences preferencias;
    private Role role;

    @Builder.Default
    private Long version = 0L;

    public static User create(String email, String encodedPassword, String nombre) {
        return User.builder()
                .id(UUID.randomUUID())
                .email(email)
                .password(encodedPassword)
                .nombre(nombre)
                .fechaCreacion(LocalDateTime.now())
                .fechaActualizacion(LocalDateTime.now())
                .role(INVITADO)
                .build();
    }

    public void actualizar(String nombre) {
        this.nombre = nombre;
        this.fechaActualizacion = LocalDateTime.now();
    }
}