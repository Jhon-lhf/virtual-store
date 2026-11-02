package com.tienda.estructuras;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pruebas de la pila de navegación (dos pilas LIFO, atrás/adelante).
 */
class PilaNavegacionTest {

    @Test
    void retrocedeEnOrdenLIFO() {
        PilaNavegacion pila = new PilaNavegacion();
        pila.visitar("A");
        pila.visitar("B");
        pila.visitar("C");

        assertEquals(Optional.of("B"), pila.irAtras());
        assertEquals(Optional.of("A"), pila.irAtras());
        assertEquals(Optional.empty(), pila.irAtras());
    }

    @Test
    void avanzaTrasRetroceder() {
        PilaNavegacion pila = new PilaNavegacion();
        pila.visitar("A");
        pila.visitar("B");
        pila.visitar("C");

        pila.irAtras();
        assertEquals(Optional.of("C"), pila.irAdelante());
        assertEquals(Optional.empty(), pila.irAdelante());
    }

    /** Visitar un producto nuevo descarta la pila de "adelante" (como un navegador). */
    @Test
    void visitarDescartaElHistorialDeAdelante() {
        PilaNavegacion pila = new PilaNavegacion();
        pila.visitar("A");
        pila.visitar("B");
        pila.irAtras();
        assertTrue(pila.puedeAdelante());

        pila.visitar("D");
        assertFalse(pila.puedeAdelante());
        assertEquals("D", pila.actual());
    }

    @Test
    void banderasDeDisponibilidad() {
        PilaNavegacion pila = new PilaNavegacion();
        assertFalse(pila.puedeAtras());
        assertFalse(pila.puedeAdelante());

        pila.visitar("A");
        assertFalse(pila.puedeAtras());

        pila.visitar("B");
        assertTrue(pila.puedeAtras());
        assertFalse(pila.puedeAdelante());
    }

    @Test
    void limpiarVaciaTodoElHistorial() {
        PilaNavegacion pila = new PilaNavegacion();
        pila.visitar("A");
        pila.visitar("B");
        pila.limpiar();

        assertFalse(pila.puedeAtras());
        assertFalse(pila.puedeAdelante());
        assertEquals(Optional.empty(), pila.irAtras());
    }
}
