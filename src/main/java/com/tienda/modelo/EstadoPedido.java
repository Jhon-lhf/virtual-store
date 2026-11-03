package com.tienda.modelo;

/**
 * Estado del ciclo de vida de un pedido.
 *
 * <p>Flujo: {@code PENDIENTE → ENVIADO → ENTREGADO}. El cliente puede
 * cancelar un pedido mientras esté {@code PENDIENTE} (se restaura el stock);
 * el vendedor lo avanza con su cola FIFO en el próximo hito.</p>
 */
public enum EstadoPedido {
    PENDIENTE,
    ENVIADO,
    ENTREGADO,
    CANCELADO
}
