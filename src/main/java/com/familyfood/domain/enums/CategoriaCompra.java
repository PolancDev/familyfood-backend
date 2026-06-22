package com.familyfood.domain.enums;

import java.util.List;

/**
 * Constantes para categorías predefinidas de la lista de compra.
 * Se usa como clase de constantes String (no enum clásico) para flexibilidad futura.
 */
public final class CategoriaCompra {

    public static final String LACTEOS = "LACTEOS";
    public static final String FRUTERIA = "FRUTERIA";
    public static final String CARNE = "CARNE";
    public static final String PESCADERIA = "PESCADERIA";
    public static final String CONGELADOS = "CONGELADOS";
    public static final String PANADERIA = "PANADERIA";
    public static final String CONSERVAS = "CONSERVAS";
    public static final String LEGUMBRES = "LEGUMBRES";
    public static final String ARROCES = "ARROCES";
    public static final String PASTA = "PASTA";
    public static final String OTROS = "OTROS";

    private CategoriaCompra() {
        // Prevent instantiation
    }

    /**
     * Valida si una categoría es una de las predefinidas.
     *
     * @param categoria la categoría a validar
     * @return true si es una categoría predefinida
     */
    public static boolean isPredefinida(String categoria) {
        return categoriasPredefinidas().contains(categoria);
    }

    /**
     * Devuelve todas las categorías predefinidas.
     *
     * @return lista con las categorías predefinidas
     */
    public static List<String> categoriasPredefinidas() {
        return List.of(LACTEOS, FRUTERIA, CARNE, PESCADERIA, CONGELADOS,
                PANADERIA, CONSERVAS, LEGUMBRES, ARROCES, PASTA, OTROS);
    }
}
