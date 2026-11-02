package com.tienda.servicio;

import com.tienda.modelo.Categoria;
import com.tienda.modelo.OrigenCategoria;
import com.tienda.modelo.Producto;
import com.tienda.persistencia.RepositorioCategorias;
import com.tienda.persistencia.RepositorioProductos;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pruebas del catálogo: búsqueda con filtros, orden, alta de productos y
 * validaciones de stock. Usa archivos JSON temporales.
 */
class ServicioProductosTest {

    private static final Path ARCHIVO_PRODUCTOS = Paths.get("data", "test-productos.json");
    private static final Path ARCHIVO_CATEGORIAS = Paths.get("data", "test-categorias.json");

    private RepositorioProductos repositorioProductos;
    private RepositorioCategorias repositorioCategorias;
    private ServicioProductos servicio;

    @BeforeEach
    void preparar() throws Exception {
        Files.deleteIfExists(ARCHIVO_PRODUCTOS);
        Files.deleteIfExists(ARCHIVO_CATEGORIAS);
        repositorioProductos = new RepositorioProductos(ARCHIVO_PRODUCTOS);
        repositorioCategorias = new RepositorioCategorias(ARCHIVO_CATEGORIAS);
        servicio = new ServicioProductos(repositorioProductos, repositorioCategorias);

        repositorioCategorias.guardar(new Categoria("C-1", "Tecnologia", "2026-01-01", OrigenCategoria.SEMILLA));
        repositorioCategorias.guardar(new Categoria("C-2", "Ropa", "2026-01-01", OrigenCategoria.SEMILLA));
        repositorioCategorias.guardar(new Categoria("C-3", "Libros", "2026-01-01", OrigenCategoria.SEMILLA));

        sembrar("P-1", "Laptop Pro 14", "Portátil con 16 GB de RAM.", 999.00, 8, "C-1");
        sembrar("P-2", "Camiseta Premium", "Algodón peinado suave.", 24.99, 40, "C-2");
        sembrar("P-3", "Auriculares Aero", "Cancelación de ruido activa.", 79.99, 3, "C-1");
        sembrar("P-4", "Código Limpio", "Manual de software.", 39.90, 0, "C-3");
    }

    @AfterEach
    void limpiar() throws Exception {
        Files.deleteIfExists(ARCHIVO_PRODUCTOS);
        Files.deleteIfExists(ARCHIVO_CATEGORIAS);
    }

    private void sembrar(String id, String titulo, String descripcion,
                         double precio, int stock, String categoriaId) {
        repositorioProductos.guardar(new Producto(id, titulo, descripcion, precio, stock,
                categoriaId, "U-vendedor", "2026-01-01"));
    }

    private ServicioProductos.CriteriosBusqueda criterios(String texto, String categoriaId,
                                                          Double min, Double max, CriterioOrden orden) {
        return new ServicioProductos.CriteriosBusqueda(texto, categoriaId, min, max, orden);
    }

    @Test
    void buscarSinFiltrosDevuelveTodos() {
        assertEquals(4, servicio.buscar(criterios(null, null, null, null, null)).size());
    }

    @Test
    void buscarPorTextoEnTitulo() {
        List<Producto> resultados = servicio.buscar(criterios("laptop", null, null, null, null));
        assertEquals(1, resultados.size());
        assertEquals("P-1", resultados.get(0).getId());
    }

    @Test
    void buscarPorTextoEnDescripcion() {
        List<Producto> resultados = servicio.buscar(criterios("ruido", null, null, null, null));
        assertEquals(1, resultados.size());
        assertEquals("P-3", resultados.get(0).getId());
    }

    @Test
    void filtrarPorCategoria() {
        List<Producto> resultados = servicio.buscar(criterios(null, "C-1", null, null, null));
        assertEquals(2, resultados.size());
    }

    @Test
    void filtrarPorRangoDePrecioInclusivo() {
        List<Producto> resultados = servicio.buscar(criterios(null, null, 30.0, 100.0, null));
        assertEquals(2, resultados.size()); // Auriculares (79.99) y Código Limpio (39.90)
    }

    @Test
    void ordenarPorPrecioAscendenteYDescendente() {
        List<Producto> ascendente = servicio.buscar(criterios(null, null, null, null, CriterioOrden.PRECIO_ASC));
        assertEquals("P-2", ascendente.get(0).getId());
        List<Producto> descendente = servicio.buscar(criterios(null, null, null, null, CriterioOrden.PRECIO_DESC));
        assertEquals("P-1", descendente.get(0).getId());
    }

    @Test
    void crearProductoGeneraIdYVinculaVendedor() {
        Producto creado = servicio.crear(
                new ServicioProductos.DatosProducto("Silla Ergonómica", "Respaldo lumbar.", 420.00, 10, "C-3"),
                "U-vendedor");
        assertTrue(creado.getId().startsWith("P-"));
        assertEquals("U-vendedor", creado.getVendedorId());
        assertNotEquals(0, servicio.contar());
        assertEquals(5, servicio.contar());
    }

    @Test
    void crearConCategoriaInexistenteFalla() {
        ExcepcionNegocio e = assertThrows(ExcepcionNegocio.class,
                () -> servicio.crear(new ServicioProductos.DatosProducto("X", "", 10, 1, "C-999"), "U-vendedor"));
        assertTrue(e.getMessage().contains("categoría"));
    }

    @Test
    void crearConPrecioInvalidoFalla() {
        assertThrows(ExcepcionNegocio.class,
                () -> servicio.crear(new ServicioProductos.DatosProducto("X", "", 0, 1, "C-1"), "U-vendedor"));
    }

    @Test
    void actualizarStockRechazaNegativos() {
        assertThrows(ExcepcionNegocio.class, () -> servicio.actualizarStock("P-1", -1));
        servicio.actualizarStock("P-1", 50);
        assertEquals(50, servicio.obtenerPorId("P-1").orElseThrow().getStock());
    }

    @Test
    void descontarStockValidaDisponibilidad() {
        assertThrows(ExcepcionNegocio.class, () -> servicio.descontarStock("P-3", 10));
        servicio.descontarStock("P-3", 3);
        assertEquals(0, servicio.obtenerPorId("P-3").orElseThrow().getStock());
    }

    @Test
    void nombreCategoriaResuelveElNombreDeLaEntidad() {
        assertEquals("Tecnologia", servicio.nombreCategoria("C-1"));
        assertEquals("—", servicio.nombreCategoria("C-999"));
    }
}
