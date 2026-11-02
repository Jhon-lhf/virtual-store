package com.tienda.modelo;

/**
 * Ciclo de vida de una solicitud de categoría enviada por un vendedor:
 * nace {@code PENDIENTE} y termina en {@code APROBADA} (crea la categoría)
 * o en {@code RECHAZADA} (no crea nada).
 */
public enum EstadoSolicitud {
    PENDIENTE,
    APROBADA,
    RECHAZADA
}
