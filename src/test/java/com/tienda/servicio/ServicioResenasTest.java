package com.tienda.servicio;

import com.tienda.modelo.Producto;
import com.tienda.persistencia.RepositorioProductos;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pruebas de reseñas: una por autor (actualizable), calificación 1..5 y
 * comentario obligatorio. El promedio lo calcula el propio Producto.
 */
class ServicioResenasTest {

    private static final Path ARCHIVO = Paths.get("data", "test-resenas.json");

    private RepositorioProductos repositorioProductos;
    private ServicioResenas servicio;

    @BeforeEach
    void preparar() throws Exception {
        Files.deleteIfExists(ARCHIVO);
        repositorioProductos = new RepositorioProductos(ARCHIVO);
        repositorioProductos.guardar(new Producto("P-1", "Laptop Pro 14", "Descripción", 999.00, 8, "C-1", "U-ven", "2026-01-01"));
        servicio = new ServicioResenas(repositorioProductos);
    }

    @AfterEach
    void limpiar() throws Exception {
        Files.deleteIfExists(ARCHIVO);
    }

    @Test
    void crearResenaYCalcularPromedio() {
        servicio.dejarResena("P-1", "U-1", "Ana", 5, "Excelente producto");
        servicio.dejarResena("P-1", "U-2", "Beto", 3, "Buena, pero costosa");

        Producto producto = repositorioProductos.buscarPorId("P-1").orElseThrow();
        assertEquals(2, producto.totalResenas());
        assertEquals(4.0, producto.calificacionPromedio());
    }

    @Test
    void reseñarDosVecesActualizaLaResenaDelAutor() {
        servicio.dejarResena("P-1", "U-1", "Ana", 2, "Llegó tarde");
        servicio.dejarResena("P-1", "U-1", "Ana", 5, "Me compensaron, excelente");

        Producto producto = repositorioProductos.buscarPorId("P-1").orElseThrow();
        assertEquals(1, producto.totalResenas());
        assertEquals(5, producto.getResenas().get(0).getCalificacion());
        assertTrue(servicio.resenaDe("P-1", "U-1").isPresent());
    }

    @Test
    void validaCalificacionYComentario() {
        assertThrows(ExcepcionNegocio.class,
                () -> servicio.dejarResena("P-1", "U-1", "Ana", 0, "Malo"));
        assertThrows(ExcepcionNegocio.class,
                () -> servicio.dejarResena("P-1", "U-1", "Ana", 6, "Increíble"));
        assertThrows(ExcepcionNegocio.class,
                () -> servicio.dejarResena("P-1", "U-1", "Ana", 5, "   "));
        assertThrows(ExcepcionNegocio.class,
                () -> servicio.dejarResena("P-999", "U-1", "Ana", 5, "Comentario"));
    }
}
