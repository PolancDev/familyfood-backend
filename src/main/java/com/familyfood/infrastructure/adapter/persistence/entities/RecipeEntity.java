package com.familyfood.infrastructure.adapter.persistence.entities;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "recipes")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class RecipeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false, length = 2000)
    private String descripcion;

    @Column(name = "tiempo_minutos", nullable = false)
    private Integer tiempoMinutos;

    @Column(nullable = false)
    private Integer raciones;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "recipe_ingredients", joinColumns = @JoinColumn(name = "recipe_id"))
    private List<RecipeIngredientEntity> ingredientes;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "recipe_pasos", joinColumns = @JoinColumn(name = "recipe_id"))
    @Column(name = "paso", length = 2000)
    private List<String> pasos;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "recipe_etiquetas", joinColumns = @JoinColumn(name = "recipe_id"))
    @Column(name = "etiqueta")
    private List<String> etiquetas;

    @Column
    private String imagen;

    @Column(nullable = false)
    private boolean favorita;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Version
    @Column(nullable = false)
    private Long version;
}
