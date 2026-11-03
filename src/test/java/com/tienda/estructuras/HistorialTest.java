package com.tienda.estructuras;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pruebas del historial de deshacer/rehacer (dos pilas LIFO).
 */
class HistorialTest {

    @Test
    void deshacerSinEstadosPreviosDevuelveVacio() {
        Historial<String> historial = new Historial<>();
        assertTrue(historial.deshacer("actual").isEmpty());
        assertFalse(historial.puedeDeshacer());
    }

    @Test
    void deshacerRestauraElEstadoAnterior() {
        Historial<String> historial = new Historial<>();
        historial.guardarEstado("estado-1");
        historial.guardarEstado("estado-2");

        assertEquals(Optional.of("estado-2"), historial.deshacer("actual"));
        assertEquals(Optional.of("estado-1"), historial.deshacer("estado-2"));
        assertTrue(historial.deshacer("estado-1").isEmpty());
    }

    @Test
    void rehacerEsElEspejoDeDeshacer() {
        Historial<String> historial = new Historial<>();
        historial.guardarEstado("anterior");

        assertEquals(Optional.of("anterior"), historial.deshacer("actual"));
        assertEquals(Optional.of("actual"), historial.rehacer("anterior"));
        assertTrue(historial.rehacer("actual").isEmpty());
    }

    @Test
    void unCambioNuevoDescartaLaPilaDeRehacer() {
        Historial<String> historial = new Historial<>();
        historial.guardarEstado("anterior");
        historial.deshacer("actual");
        assertTrue(historial.puedeRehacer());

        // Un estado nuevo invalida el "futuro" pendiente, como en un editor.
        historial.guardarEstado("actual");
        assertFalse(historial.puedeRehacer());
    }

    @Test
    void limpiarVaciaAmbasPilas() {
        Historial<String> historial = new Historial<>();
        historial.guardarEstado("anterior");
        historial.deshacer("actual");
        historial.limpiar();
        assertFalse(historial.puedeDeshacer());
        assertFalse(historial.puedeRehacer());
    }
}
