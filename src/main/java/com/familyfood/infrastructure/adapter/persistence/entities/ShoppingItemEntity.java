package com.familyfood.infrastructure.adapter.persistence.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "shopping_items")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class ShoppingItemEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shopping_list_id", nullable = false)
    private ShoppingListEntity shoppingList;

    @Column(nullable = false)
    private String ingrediente;

    @Column(nullable = false)
    private Double cantidad;

    @Column(nullable = false, length = 50)
    private String unidad;

    @Column(nullable = false, length = 50)
    private String categoria;

    @Column(nullable = false)
    private boolean comprado;

    @Column(name = "es_manual", nullable = false)
    private boolean esManual;

    @Version
    @Column(nullable = false)
    private Long version;
}
