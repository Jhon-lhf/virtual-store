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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pruebas del carrito: validaciones de stock, totales con el impuesto del
 * 19% y el ciclo deshacer/rehacer.
 */
class ServicioCarritoTest {

    private static final Path ARCHIVO = Paths.get("data", "test-carrito.json");

    private RepositorioProductos repositorioProductos;
    private SesionActual sesion;
    private ServicioCarrito servicio;

    @BeforeEach
    void preparar() throws Exception {
        Files.deleteIfExists(ARCHIVO);
        repositorioProductos = new RepositorioProductos(ARCHIVO);
        sembrar("P-1", "Laptop Pro 14", 999.00, 8);
        sembrar("P-2", "Camiseta Premium", 24.99, 40);
        sembrar("P-3", "Auriculares Aero", 79.99, 3);
        sembrar("P-4", "Producto Agotado", 19.99, 0);
        sesion = new SesionActual();
        servicio = new ServicioCarrito(repositorioProductos, sesion);
    }

    @AfterEach
    void limpiar() throws Exception {
        Files.deleteIfExists(ARCHIVO);
    }

    private void sembrar(String id, String titulo, double precio, int stock) {
        repositorioProductos.guardar(new Producto(id, titulo, "Descripción", precio, stock, "C-1", "U-ven", "2026-01-01"));
    }

    @Test
    void agregarAcumulaCantidadesYCalculaTotalesConImpuesto() {
        servicio.agregar("P-1", 2);
        servicio.agregar("P-2", 1);
        servicio.agregar("P-1", 1); // se acumula con lo ya agregado

        assertEquals(4, servicio.cantidadItems());
        ServicioCarrito.Totales totales = servicio.calcularTotales();
        assertEquals(3021.99, totales.subtotal()); // 999*3 + 24.99
        assertEquals(574.18, totales.impuesto());  // 19% de 3021.99
        assertEquals(3596.17, totales.total());
    }

    @Test
    void agregarValidaCantidadStockYAgotados() {
        ExcepcionNegocio cantidadInvalida = assertThrows(ExcepcionNegocio.class,
                () -> servicio.agregar("P-1", 0));
        assertEquals("La cantidad debe ser mayor que cero", cantidadInvalida.getMessage());

        ExcepcionNegocio stockInsuficiente = assertThrows(ExcepcionNegocio.class,
                () -> servicio.agregar("P-1", 9));
        assertTrue(stockInsuficiente.getMessage().contains("Solo quedan 8"));

        // Lo ya agregado también consume stock.
        servicio.agregar("P-3", 2);
        assertThrows(ExcepcionNegocio.class, () -> servicio.agregar("P-3", 2));

        ExcepcionNegocio agotado = assertThrows(ExcepcionNegocio.class,
                () -> servicio.agregar("P-4", 1));
        assertTrue(agotado.getMessage().contains("agotado"));
    }

    @Test
    void cambiarCantidadValidaElStockDisponible() {
        servicio.agregar("P-3", 2);
        assertThrows(ExcepcionNegocio.class, () -> servicio.cambiarCantidad("P-3", 4));
        servicio.cambiarCantidad("P-3", 3);
        assertEquals(3, servicio.cantidadItems());
        assertThrows(ExcepcionNegocio.class, () -> servicio.cambiarCantidad("P-1", 5));
    }

    @Test
    void quitarYVaciarDejanElCarritoVacio() {
        servicio.agregar("P-1", 1);
        servicio.agregar("P-2", 2);
        servicio.quitar("P-1");
        assertEquals(2, servicio.cantidadItems());
        assertThrows(ExcepcionNegocio.class, () -> servicio.quitar("P-1"));
        servicio.vaciar();
        assertEquals(0, servicio.cantidadItems());
    }

    @Test
    void deshacerYRehacerRestauranEstadosDelCarrito() {
        servicio.agregar("P-1", 2);
        servicio.quitar("P-1");
        assertEquals(0, servicio.cantidadItems());
        assertTrue(servicio.puedeDeshacer());
        assertFalse(servicio.puedeRehacer());

        assertTrue(servicio.deshacer());
        assertEquals(2, servicio.cantidadItems());
        assertTrue(servicio.puedeRehacer());

        assertTrue(servicio.rehacer());
        assertEquals(0, servicio.cantidadItems());

        // El ciclo puede repetirse indefinidamente mientras no haya cambios nuevos.
        assertTrue(servicio.deshacer());
        assertEquals(2, servicio.cantidadItems());
    }

    @Test
    void reiniciarBorraCarritoEHistorial() {
        servicio.agregar("P-1", 2);
        servicio.reiniciar();
        assertEquals(0, servicio.cantidadItems());
        assertFalse(servicio.puedeDeshacer());
    }
}
