package com.tienda.servicio;

import com.tienda.modelo.Producto;
import com.tienda.persistencia.RepositorioDeseos;
import com.tienda.persistencia.RepositorioProductos;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pruebas de la lista de deseos: alta idempotente, consulta y baja.
 */
class ServicioDeseosTest {

    private static final Path ARCHIVO_DESEOS = Paths.get("data", "test-deseos.json");
    private static final Path ARCHIVO_PRODUCTOS = Paths.get("data", "test-deseos-productos.json");

    private RepositorioDeseos repositorioDeseos;
    private RepositorioProductos repositorioProductos;
    private ServicioDeseos servicio;

    @BeforeEach
    void preparar() throws Exception {
        Files.deleteIfExists(ARCHIVO_DESEOS);
        Files.deleteIfExists(ARCHIVO_PRODUCTOS);
        repositorioDeseos = new RepositorioDeseos(ARCHIVO_DESEOS);
        repositorioProductos = new RepositorioProductos(ARCHIVO_PRODUCTOS);
        repositorioProductos.guardar(new Producto("P-1", "Laptop Pro 14", "Descripción", 999.00, 8, "C-1", "U-ven", "2026-01-01"));
        repositorioProductos.guardar(new Producto("P-2", "Camiseta Premium", "Descripción", 24.99, 40, "C-2", "U-ven", "2026-01-01"));
        servicio = new ServicioDeseos(repositorioDeseos, repositorioProductos);
    }

    @AfterEach
    void limpiar() throws Exception {
        Files.deleteIfExists(ARCHIVO_DESEOS);
        Files.deleteIfExists(ARCHIVO_PRODUCTOS);
    }

    @Test
    void agregarQuitarYConsultar() {
        assertFalse(servicio.esDeseado("U-1", "P-1"));

        servicio.agregar("U-1", "P-1");
        assertTrue(servicio.esDeseado("U-1", "P-1"));
        assertEquals(1, servicio.listarProductos("U-1").size());

        servicio.quitar("U-1", "P-1");
        assertFalse(servicio.esDeseado("U-1", "P-1"));
        assertTrue(servicio.listarProductos("U-1").isEmpty());
    }

    @Test
    void agregarDosVecesNoDuplica() {
        servicio.agregar("U-1", "P-1");
        servicio.agregar("U-1", "P-1");
        List<Producto> productos = servicio.listarProductos("U-1");
        assertEquals(1, productos.size());
    }

    @Test
    void cadaUsuarioTieneSuPropiaLista() {
        servicio.agregar("U-1", "P-1");
        servicio.agregar("U-2", "P-2");
        assertEquals(1, servicio.listarProductos("U-1").size());
        assertEquals(1, servicio.listarProductos("U-2").size());
        assertFalse(servicio.esDeseado("U-1", "P-2"));
    }

    @Test
    void agregarProductoInexistenteFalla() {
        assertThrows(ExcepcionNegocio.class, () -> servicio.agregar("U-1", "P-999"));
    }

    @Test
    void losProductosEliminadosDelCatalogoSeDescartanDeLaVista() {
        servicio.agregar("U-1", "P-1");
        repositorioProductos.eliminarPorId("P-1");
        assertTrue(servicio.listarProductos("U-1").isEmpty());
    }
}
