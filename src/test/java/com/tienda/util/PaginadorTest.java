package com.tienda.util;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pruebas del corte puro de paginación (con clamp).
 */
class PaginadorTest {

    @Test
    void cortaLaPrimeraPagina() {
        List<Integer> datos = List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20);
        Pagina<Integer> pagina = Paginador.paginar(datos, 1, 8);
        assertEquals(List.of(1, 2, 3, 4, 5, 6, 7, 8), pagina.elementos());
        assertEquals(1, pagina.numero());
        assertEquals(3, pagina.totalPaginas());
        assertEquals(20, pagina.totalElementos());
    }

    @Test
    void cortaUnaPaginaIntermedia() {
        List<Integer> datos = List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20);
        Pagina<Integer> pagina = Paginador.paginar(datos, 2, 8);
        assertEquals(List.of(9, 10, 11, 12, 13, 14, 15, 16), pagina.elementos());
    }

    /** Clamp: pedir una página inexistente devuelve la última válida. */
    @Test
    void ajustaPaginasFueraDeRangoHaciaAbajo() {
        List<Integer> datos = List.of(1, 2, 3, 4, 5, 6, 7, 8, 9);
        Pagina<Integer> pagina = Paginador.paginar(datos, 99, 8);
        assertEquals(2, pagina.numero());
        assertEquals(List.of(9), pagina.elementos());
    }

    /** Clamp: páginas cero o negativas vuelven a la primera. */
    @Test
    void ajustaPaginasInvalidasHaciaArriba() {
        List<Integer> datos = List.of(1, 2, 3);
        assertEquals(1, Paginador.paginar(datos, 0, 8).numero());
        assertEquals(1, Paginador.paginar(datos, -5, 8).numero());
    }

    @Test
    void listaVaciaDevuelveUnaPaginaVacia() {
        Pagina<Integer> pagina = Paginador.paginar(List.of(), 1, 8);
        assertEquals(1, pagina.totalPaginas());
        assertTrue(pagina.elementos().isEmpty());
        assertEquals(0, pagina.totalElementos());
    }

    @Test
    void paginaExactaNoGeneraPaginaExtra() {
        List<Integer> datos = List.of(1, 2, 3, 4, 5, 6, 7, 8);
        assertEquals(1, Paginador.paginar(datos, 1, 8).totalPaginas());
    }
}
