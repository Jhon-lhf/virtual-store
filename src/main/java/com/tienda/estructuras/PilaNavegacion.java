package com.tienda.estructuras;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Optional;

/**
 * Historial de navegación con dos pilas LIFO (una de "atrás" y una de
 * "adelante"), como el historial de un navegador web.
 *
 * <p><b>LIFO (Last-In, First-Out)</b>: el último ID apilado es el primero en
 * salir, que es exactamente el comportamiento esperado al retroceder.</p>
 *
 * <p>Uso previsto: el catálogo apila el producto visitado en el detalle
 * ({@link #visitar}); los botones {@code < Atrás} / {@code Adelante >} navegan
 * con {@link #irAtras()} / {@link #irAdelante()}. Visitar un producto nuevo
 * descarta la pila de "adelante" (igual que un navegador).</p>
 */
public class PilaNavegacion {

    private final Deque<String> pilaAtras = new ArrayDeque<>();
    private final Deque<String> pilaAdelante = new ArrayDeque<>();
    private String actual;

    /** Registra una visita a un ID; descarta la pila de "adelante". */
    public void visitar(String id) {
        if (actual != null) {
            pilaAtras.push(actual);
        }
        pilaAdelante.clear();
        actual = id;
    }

    /** Retrocede una posición (LIFO); vacío si no hay historial previo. */
    public Optional<String> irAtras() {
        if (pilaAtras.isEmpty()) {
            return Optional.empty();
        }
        pilaAdelante.push(actual);
        actual = pilaAtras.pop();
        return Optional.of(actual);
    }

    /** Avanza una posición (LIFO); vacío si no se ha retrocedido antes. */
    public Optional<String> irAdelante() {
        if (pilaAdelante.isEmpty()) {
            return Optional.empty();
        }
        pilaAtras.push(actual);
        actual = pilaAdelante.pop();
        return Optional.of(actual);
    }

    public boolean puedeAtras() {
        return !pilaAtras.isEmpty();
    }

    public boolean puedeAdelante() {
        return !pilaAdelante.isEmpty();
    }

    /** ID actual, o null si aún no se ha visitado nada. */
    public String actual() {
        return actual;
    }

    /** Limpia todo el historial (ej. al cerrar sesión). */
    public void limpiar() {
        pilaAtras.clear();
        pilaAdelante.clear();
        actual = null;
    }
}
