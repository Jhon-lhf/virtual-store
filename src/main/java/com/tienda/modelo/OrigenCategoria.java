package com.tienda.modelo;

/**
 * Cómo nació una categoría dentro del sistema.
 *
 * <ul>
 *   <li>{@code SEMILLA}: datos iniciales con los que arranca la aplicación.</li>
 *   <li>{@code ADMINISTRADOR}: creada manualmente por un administrador.</li>
 *   <li>{@code SOLICITUD}: propuesta por un vendedor y aprobada por un admin.</li>
 * </ul>
 */
public enum OrigenCategoria {
    SEMILLA,
    ADMINISTRADOR,
    SOLICITUD
}
