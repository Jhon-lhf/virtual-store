package com.tienda.servicio;

import com.tienda.modelo.CarritoItem;
import com.tienda.modelo.EstadoPedido;
import com.tienda.modelo.Pedido;
import com.tienda.modelo.Producto;
import com.tienda.persistencia.RepositorioPedidos;
import com.tienda.persistencia.RepositorioProductos;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pruebas del checkout: validación todo-o-nada del stock, descuento de
 * inventario, totales del pedido y cancelación con restauración de stock.
 */
class ServicioPedidosTest {

    private static final Path ARCHIVO_PRODUCTOS = Paths.get("data", "test-pedidos-productos.json");
    private static final Path ARCHIVO_PEDIDOS = Paths.get("data", "test-pedidos.json");

    private RepositorioProductos repositorioProductos;
    private RepositorioPedidos repositorioPedidos;
    private ServicioPedidos servicio;

    @BeforeEach
    void preparar() throws Exception {
        Files.deleteIfExists(ARCHIVO_PRODUCTOS);
        Files.deleteIfExists(ARCHIVO_PEDIDOS);
        repositorioProductos = new RepositorioProductos(ARCHIVO_PRODUCTOS);
        repositorioPedidos = new RepositorioPedidos(ARCHIVO_PEDIDOS);
        servicio = new ServicioPedidos(repositorioPedidos, repositorioProductos);

        repositorioProductos.guardar(new Producto("P-1", "Laptop Pro 14", "Descripción", 999.00, 8, "C-1", "U-ven", "2026-01-01"));
        repositorioProductos.guardar(new Producto("P-2", "Camiseta Premium", "Descripción", 24.99, 40, "C-2", "U-ven", "2026-01-01"));
        repositorioProductos.guardar(new Producto("P-3", "Auriculares Aero", "Descripción", 79.99, 3, "C-1", "U-ven", "2026-01-01"));
    }

    @AfterEach
    void limpiar() throws Exception {
        Files.deleteIfExists(ARCHIVO_PRODUCTOS);
        Files.deleteIfExists(ARCHIVO_PEDIDOS);
    }

    @Test
    void crearPedidoValidaDescuentaYFotografiaPrecios() {
        Pedido pedido = servicio.crearPedido(
                List.of(new CarritoItem("P-1", 2), new CarritoItem("P-2", 3)),
                "U-cli", "Cliente Demo");

        assertEquals(EstadoPedido.PENDIENTE, pedido.getEstado());
        assertEquals("U-cli", pedido.getClienteId());
        assertEquals(2072.97, pedido.getSubtotal()); // 999*2 + 24.99*3
        assertEquals(393.86, pedido.getImpuesto());
        assertEquals(2466.83, pedido.getTotal());
        assertEquals(2, pedido.getItems().size());
        assertEquals("Laptop Pro 14", pedido.getItems().get(0).getTitulo());
        assertEquals(999.00, pedido.getItems().get(0).getPrecioUnitario());

        assertEquals(6, repositorioProductos.buscarPorId("P-1").orElseThrow().getStock());
        assertEquals(37, repositorioProductos.buscarPorId("P-2").orElseThrow().getStock());
    }

    @Test
    void carritoVacioNoGeneraPedido() {
        ExcepcionNegocio excepcion = assertThrows(ExcepcionNegocio.class,
                () -> servicio.crearPedido(List.of(), "U-cli", "Cliente Demo"));
        assertEquals("El carrito está vacío", excepcion.getMessage());
    }

    @Test
    void laCompraEsTodoONadaSiFaltaStock() {
        ExcepcionNegocio excepcion = assertThrows(ExcepcionNegocio.class,
                () -> servicio.crearPedido(
                        List.of(new CarritoItem("P-1", 2), new CarritoItem("P-3", 5)),
                        "U-cli", "Cliente Demo"));
        assertTrue(excepcion.getMessage().contains("Stock insuficiente"));

        // Nada se descontó: la validación falló antes de mutar.
        assertEquals(8, repositorioProductos.buscarPorId("P-1").orElseThrow().getStock());
        assertEquals(3, repositorioProductos.buscarPorId("P-3").orElseThrow().getStock());
    }

    @Test
    void cancelarRestauraElStockYMarcaCancelado() {
        Pedido pedido = servicio.crearPedido(List.of(new CarritoItem("P-1", 2)), "U-cli", "Cliente Demo");
        assertEquals(6, repositorioProductos.buscarPorId("P-1").orElseThrow().getStock());

        servicio.cancelar(pedido.getId(), "U-cli");

        assertEquals(EstadoPedido.CANCELADO,
                repositorioPedidos.buscarPorId(pedido.getId()).orElseThrow().getEstado());
        assertEquals(8, repositorioProductos.buscarPorId("P-1").orElseThrow().getStock());
    }

    @Test
    void soloElDuenioPuedeCancelarYPedidoEnviadoNoSeCancela() {
        Pedido pedido = servicio.crearPedido(List.of(new CarritoItem("P-1", 1)), "U-cli", "Cliente Demo");

        assertThrows(ExcepcionNegocio.class, () -> servicio.cancelar(pedido.getId(), "U-otro"));

        pedido.setEstado(EstadoPedido.ENVIADO);
        repositorioPedidos.guardar(pedido);
        ExcepcionNegocio excepcion = assertThrows(ExcepcionNegocio.class,
                () -> servicio.cancelar(pedido.getId(), "U-cli"));
        assertEquals("Solo se pueden cancelar pedidos pendientes", excepcion.getMessage());
    }

    @Test
    void listarPorClienteSoloDevuelveLosPropios() {
        servicio.crearPedido(List.of(new CarritoItem("P-1", 1)), "U-cli", "Cliente Demo");
        servicio.crearPedido(List.of(new CarritoItem("P-2", 1)), "U-otro", "Otro Cliente");
        servicio.crearPedido(List.of(new CarritoItem("P-3", 1)), "U-cli", "Cliente Demo");

        List<Pedido> propios = servicio.listarPorCliente("U-cli");
        assertEquals(2, propios.size());
        // El más reciente primero.
        assertTrue(propios.get(0).getFecha().compareTo(propios.get(1).getFecha()) >= 0);
    }
}
