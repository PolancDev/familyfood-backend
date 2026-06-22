package com.familyfood.domain.model;

import com.familyfood.domain.enums.CategoriaCompra;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShoppingItem {
    private UUID id;
    private UUID shoppingListId;
    private String ingrediente;
    private Double cantidad;
    private String unidad;
    private String categoria;
    private boolean comprado;
    private boolean esManual;

    @Builder.Default
    private Long version = 0L;

    public void marcarComprado() {
        this.comprado = true;
    }

    public void desmarcarComprado() {
        this.comprado = false;
    }

    public static ShoppingItem crearManual(UUID shoppingListId, String ingrediente,
                                            Double cantidad, String unidad, String categoria) {
        return ShoppingItem.builder()
                .id(UUID.randomUUID())
                .shoppingListId(shoppingListId)
                .ingrediente(ingrediente)
                .cantidad(cantidad)
                .unidad(unidad)
                .categoria(categoria != null ? categoria : CategoriaCompra.OTROS)
                .comprado(false)
                .esManual(true)
                .build();
    }
}
