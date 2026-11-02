package com.tienda.servicio;

import com.tienda.modelo.Categoria;
import com.tienda.modelo.EstadoSolicitud;
import com.tienda.modelo.OrigenCategoria;
import com.tienda.modelo.Producto;
import com.tienda.modelo.Rol;
import com.tienda.modelo.SolicitudCategoria;
import com.tienda.modelo.Usuario;
import com.tienda.persistencia.RepositorioCategorias;
import com.tienda.persistencia.RepositorioProductos;
import com.tienda.persistencia.RepositorioSolicitudesCategoria;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pruebas del flujo de categorías: solicitud del vendedor, aprobación o
 * rechazo del admin, creación directa y visibilidad en el catálogo del cliente.
 */
class ServicioCategoriasTest {

    private static final Path ARCHIVO_CATEGORIAS = Paths.get("data", "test-categorias.json");
    private static final Path ARCHIVO_SOLICITUDES = Paths.get("data", "test-solicitudes-categoria.json");
    private static final Path ARCHIVO_PRODUCTOS = Paths.get("data", "test-productos.json");

    private RepositorioCategorias repositorioCategorias;
    private RepositorioSolicitudesCategoria repositorioSolicitudes;
    private RepositorioProductos repositorioProductos;
    private ServicioCategorias servicio;
    private Usuario vendedor;

    @BeforeEach
    void preparar() throws Exception {
        Files.deleteIfExists(ARCHIVO_CATEGORIAS);
        Files.deleteIfExists(ARCHIVO_SOLICITUDES);
        Files.deleteIfExists(ARCHIVO_PRODUCTOS);

        repositorioCategorias = new RepositorioCategorias(ARCHIVO_CATEGORIAS);
        repositorioSolicitudes = new RepositorioSolicitudesCategoria(ARCHIVO_SOLICITUDES);
        repositorioProductos = new RepositorioProductos(ARCHIVO_PRODUCTOS);
        servicio = new ServicioCategorias(repositorioCategorias, repositorioSolicitudes, repositorioProductos);

        repositorioCategorias.guardar(new Categoria("C-1", "Tecnologia", "2026-01-01", OrigenCategoria.SEMILLA));
        vendedor = new Usuario("U-1", "vendedor", "hash", "Vendedor Demo", "v@tienda.com", Rol.VENDEDOR, true);
    }

    @AfterEach
    void limpiar() throws Exception {
        Files.deleteIfExists(ARCHIVO_CATEGORIAS);
        Files.deleteIfExists(ARCHIVO_SOLICITUDES);
        Files.deleteIfExists(ARCHIVO_PRODUCTOS);
    }

    @Test
    void solicitarCreaSolicitudPendiente() {
        SolicitudCategoria solicitud = servicio.solicitar("Jardinería", "Productos de exteriores.", vendedor);
        assertEquals(EstadoSolicitud.PENDIENTE, solicitud.getEstado());
        assertEquals(1, servicio.listarPendientes().size());
        assertTrue(solicitud.getId().startsWith("S-"));
    }

    @Test
    void solicitarSinJustificacionFalla() {
        assertThrows(ExcepcionNegocio.class,
                () -> servicio.solicitar("Jardinería", "   ", vendedor));
    }

    @Test
    void solicitarCategoriaExistenteFalla() {
        ExcepcionNegocio e = assertThrows(ExcepcionNegocio.class,
                () -> servicio.solicitar("tecnologia", "Otra más.", vendedor));
        assertTrue(e.getMessage().contains("ya existe"));
    }

    @Test
    void solicitarDuplicadoPendienteFalla() {
        servicio.solicitar("Jardinería", "Productos de exteriores.", vendedor);
        assertThrows(ExcepcionNegocio.class,
                () -> servicio.solicitar("jardinería", "Igual pero en minúsculas.", vendedor));
    }

    @Test
    void aprobarCreaCategoriaYQuedaDisponibleParaPublicar() {
        SolicitudCategoria solicitud = servicio.solicitar("Jardinería", "Productos de exteriores.", vendedor);
        Categoria creada = servicio.aprobar(solicitud.getId());

        assertNotNull(creada);
        assertEquals("Jardinería", creada.getNombre());
        assertEquals(OrigenCategoria.SOLICITUD, creada.getOrigen());
        assertTrue(creada.getId().startsWith("C-"));
        assertEquals(EstadoSolicitud.APROBADA,
                repositorioSolicitudes.buscarPorId(solicitud.getId()).orElseThrow().getEstado());
        assertTrue(servicio.listarParaPublicar().stream()
                .anyMatch(c -> c.getNombre().equals("Jardinería")));
        assertEquals(0, servicio.listarPendientes().size());
    }

    @Test
    void rechazarNoCreaCategoria() {
        SolicitudCategoria solicitud = servicio.solicitar("Jardinería", "Productos de exteriores.", vendedor);
        servicio.rechazar(solicitud.getId());

        assertEquals(EstadoSolicitud.RECHAZADA,
                repositorioSolicitudes.buscarPorId(solicitud.getId()).orElseThrow().getEstado());
        assertTrue(servicio.listarParaPublicar().stream()
                .noneMatch(c -> c.getNombre().equals("Jardinería")));
    }

    @Test
    void unaSolicitudSoloSeResuelveUnaVez() {
        SolicitudCategoria solicitud = servicio.solicitar("Jardinería", "Productos de exteriores.", vendedor);
        servicio.aprobar(solicitud.getId());
        assertThrows(ExcepcionNegocio.class, () -> servicio.rechazar(solicitud.getId()));
    }

    @Test
    void crearDirectaUsaOrigenAdministrador() {
        Categoria creada = servicio.crearDirecta("Deportes");
        assertEquals(OrigenCategoria.ADMINISTRADOR, creada.getOrigen());
    }

    @Test
    void listarActivasConProductosSoloDevuelveCategoriasEnUso() {
        // Tecnologia tiene productos; "Deportes" no tiene ninguno.
        servicio.crearDirecta("Deportes");
        repositorioProductos.guardar(new Producto("P-1", "Laptop", "", 999, 5, "C-1", "U-1", "2026-01-01"));

        assertEquals(1, servicio.listarActivasConProductos().size());
        assertEquals("Tecnologia", servicio.listarActivasConProductos().get(0).getNombre());
    }

    @Test
    void contarProductosPorCategoria() {
        repositorioProductos.guardar(new Producto("P-1", "Laptop", "", 999, 5, "C-1", "U-1", "2026-01-01"));
        repositorioProductos.guardar(new Producto("P-2", "Mouse", "", 25, 5, "C-1", "U-1", "2026-01-01"));
        assertEquals(2, servicio.contarProductos("C-1"));
    }
}
