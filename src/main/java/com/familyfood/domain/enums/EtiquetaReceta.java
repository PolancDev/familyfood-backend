package com.familyfood.domain.enums;

/**
 * Constantes para etiquetas predefinidas de recetas.
 * El campo etiquetas en Recipe es {@code List<String>}, pero estas constantes
 * sirven como referencia para las etiquetas conocidas del sistema.
 */
public final class EtiquetaReceta {

    public static final String RAPIDA = "RAPIDA";
    public static final String ECONOMICA = "ECONOMICA";
    public static final String NINOS = "NINOS";

    private EtiquetaReceta() {
        // Prevent instantiation
    }

    /**
     * Valida si una etiqueta es una de las predefinidas.
     *
     * @param etiqueta la etiqueta a validar
     * @return true si es una etiqueta predefinida
     */
    public static boolean isPredefinida(String etiqueta) {
        return RAPIDA.equals(etiqueta) || ECONOMICA.equals(etiqueta) || NINOS.equals(etiqueta);
    }

    /**
     * Devuelve todas las etiquetas predefinidas.
     *
     * @return array con las etiquetas predefinidas
     */
    public static String[] predefinidas() {
        return new String[]{RAPIDA, ECONOMICA, NINOS};
    }
}