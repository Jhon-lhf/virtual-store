package com.tienda.estructuras;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Optional;

/**
 * Historial genérico de <b>deshacer/rehacer</b> con dos pilas LIFO, el mismo
 * patrón de {@link PilaNavegacion} pero para estados en lugar de rutas.
 *
 * <p>Cómo funciona con el carrito: antes de cada cambio se guarda una
 * <i>foto</i> del estado anterior en la pila de deshacer (y se descarta la
 * pila de rehacer, como en cualquier editor). Deshacer mueve la foto actual
 * a la pila de rehacer y restaura la anterior; rehacer es el espejo.</p>
 *
 * <p><b>LIFO (Last-In, First-Out)</b>: el último estado guardado es el
 * primero en restaurarse, que es justo lo que espera el usuario al pulsar
 * Ctrl+Z repetidamente.</p>
 *
 * @param <T> tipo del estado que se fotografía (en el carrito, la lista de ítems).
 */
public class Historial<T> {

    private final Deque<T> pilaDeshacer = new ArrayDeque<>();
    private final Deque<T> pilaRehacer = new ArrayDeque<>();

    /**
     * Guarda un estado previo antes de aplicar un cambio; descarta la pila
     * de rehacer (un cambio nuevo invalida el futuro que había quedado pendiente).
     */
    public void guardarEstado(T estado) {
        pilaDeshacer.push(estado);
        pilaRehacer.clear();
    }

    /**
     * Deshace: recibe el estado actual, lo apila en "rehacer" y devuelve el
     * anterior. Vacío si no hay nada que deshacer.
     */
    public Optional<T> deshacer(T estadoActual) {
        if (pilaDeshacer.isEmpty()) {
            return Optional.empty();
        }
        pilaRehacer.push(estadoActual);
        return Optional.of(pilaDeshacer.pop());
    }

    /** Rehace: espejo exacto de {@link #deshacer}. Vacío si no se ha deshecho nada. */
    public Optional<T> rehacer(T estadoActual) {
        if (pilaRehacer.isEmpty()) {
            return Optional.empty();
        }
        pilaDeshacer.push(estadoActual);
        return Optional.of(pilaRehacer.pop());
    }

    public boolean puedeDeshacer() {
        return !pilaDeshacer.isEmpty();
    }

    public boolean puedeRehacer() {
        return !pilaRehacer.isEmpty();
    }

    /** Limpia ambas pilas (ej. al cerrar sesión o finalizar la compra). */
    public void limpiar() {
        pilaDeshacer.clear();
        pilaRehacer.clear();
    }
}
