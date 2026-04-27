package com.familyfood.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Preferences {
    private Integer miembrosFamilia;
    private String nivelCocina;
    private String restriccionesAlimentarias;
}