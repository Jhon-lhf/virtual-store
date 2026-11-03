package com.tienda.servicio;

import com.tienda.estructuras.Historial;
import com.tienda.modelo.CarritoItem;
import com.tienda.modelo.Producto;
import com.tienda.persistencia.RepositorioProductos;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Lógica del carrito de compras de la sesión.
 *
 * <p>El carrito vive en {@link SesionActual} (transitorio) y cada cambio
 * queda fotografiado en un {@link Historial} para deshacer/rehacer con
 * Ctrl+Z / Ctrl+Y. El precio se lee siempre del producto vigente, así que
 * los totales reflejan el valor actual del catálogo.</p>
 *
 * <p>Impuesto: 19% sobre el subtotal (IVA colombiano).</p>
 */
public class ServicioCarrito {

    /** Porcentaje de impuesto aplicado al subtotal (19%). */
    public static final double TASA_IMPUESTO = 0.19;

    private final RepositorioProductos repositorioProductos;
    private final SesionActual sesion;
    private final Historial<List<CarritoItem>> historial = new Historial<>();

    public ServicioCarrito(RepositorioProductos repositorioProductos, SesionActual sesion) {
        this.repositorioProductos = repositorioProductos;
        this.sesion = sesion;
    }

    /** Totales del carrito (valores redondeados a 2 decimales). */
    public record Totales(double subtotal, double impuesto, double total) {
    }

    /** Ítem del carrito resuelto con su producto vigente. */
    public record ItemDetallado(Producto producto, int cantidad) {
    }

    // ---------------------------------------------------------------- consultas

    /** Ítems resueltos con su producto; los productos eliminados se descartan. */
    public List<ItemDetallado> itemsDetallados() {
        List<ItemDetallado> detallados = new ArrayList<>();
        for (CarritoItem item : sesion.getCarrito()) {
            Optional<Producto> producto = repositorioProductos.buscarPorId(item.getProductoId());
            producto.ifPresent(p -> detallados.add(new ItemDetallado(p, item.getCantidad())));
        }
        return detallados;
    }

    /** Cantidad total de unidades en el carrito (para el badge de la barra lateral). */
    public int cantidadItems() {
        return sesion.getCarrito().stream().mapToInt(CarritoItem::getCantidad).sum();
    }

    public Totales calcularTotales() {
        double subtotal = itemsDetallados().stream()
                .mapToDouble(i -> i.producto().getPrecio() * i.cantidad())
                .sum();
        double impuesto = redondear(subtotal * TASA_IMPUESTO);
        return new Totales(redondear(subtotal), impuesto, redondear(subtotal + impuesto));
    }

    public boolean puedeDeshacer() {
        return historial.puedeDeshacer();
    }

    public boolean puedeRehacer() {
        return historial.puedeRehacer();
    }

    // ---------------------------------------------------------------- mutaciones

    /**
     * Agrega unidades de un producto al carrito.
     *
     * @throws ExcepcionNegocio si el producto no existe, está agotado, la
     *                          cantidad no es positiva o supera el stock
     *                          disponible (contando lo ya agregado).
     */
    public void agregar(String productoId, int cantidad) {
        if (cantidad <= 0) {
            throw new ExcepcionNegocio("La cantidad debe ser mayor que cero");
        }
        Producto producto = obtenerProducto(productoId);
        if (producto.estaAgotado()) {
            throw new ExcepcionNegocio("\"" + producto.getTitulo() + "\" está agotado");
        }
        int yaEnCarrito = cantidadDe(productoId);
        if (yaEnCarrito + cantidad > producto.getStock()) {
            throw new ExcepcionNegocio("Solo quedan " + producto.getStock()
                    + " unidades de \"" + producto.getTitulo() + "\"");
        }
        fotografiar();
        CarritoItem item = buscarItem(productoId).orElseGet(() -> {
            CarritoItem nuevo = new CarritoItem(productoId, 0);
            sesion.getCarrito().add(nuevo);
            return nuevo;
        });
        item.setCantidad(item.getCantidad() + cantidad);
    }

    /**
     * Cambia la cantidad de un ítem ya agregado.
     *
     * @throws ExcepcionNegocio si el ítem no está en el carrito, la cantidad
     *                          no es positiva o supera el stock disponible.
     */
    public void cambiarCantidad(String productoId, int nuevaCantidad) {
        CarritoItem item = buscarItem(productoId)
                .orElseThrow(() -> new ExcepcionNegocio("El producto no está en el carrito"));
        if (nuevaCantidad <= 0) {
            throw new ExcepcionNegocio("La cantidad debe ser mayor que cero");
        }
        Producto producto = obtenerProducto(productoId);
        if (nuevaCantidad > producto.getStock()) {
            throw new ExcepcionNegocio("Solo quedan " + producto.getStock()
                    + " unidades de \"" + producto.getTitulo() + "\"");
        }
        fotografiar();
        item.setCantidad(nuevaCantidad);
    }

    /** Quita un ítem del carrito (error amable si no estaba). */
    public void quitar(String productoId) {
        boolean existia = buscarItem(productoId).isPresent();
        if (!existia) {
            throw new ExcepcionNegocio("El producto no está en el carrito");
        }
        fotografiar();
        sesion.getCarrito().removeIf(i -> i.getProductoId().equals(productoId));
    }

    /** Vacía el carrito completo (queda en el historial para deshacer). */
    public void vaciar() {
        if (sesion.getCarrito().isEmpty()) {
            return;
        }
        fotografiar();
        sesion.getCarrito().clear();
    }

    /** Deshace el último cambio; devuelve true si aplicó algo. */
    public boolean deshacer() {
        List<CarritoItem> restaurado = historial.deshacer(copiaCarrito()).orElse(null);
        if (restaurado == null) {
            return false;
        }
        reemplazarCarrito(restaurado);
        return true;
    }

    /** Rehace el último cambio deshecho; devuelve true si aplicó algo. */
    public boolean rehacer() {
        List<CarritoItem> restaurado = historial.rehacer(copiaCarrito()).orElse(null);
        if (restaurado == null) {
            return false;
        }
        reemplazarCarrito(restaurado);
        return true;
    }

    /**
     * Reinicia carrito e historial: se usa al iniciar o cerrar sesión, y al
     * finalizar la compra (el pedido ya no se puede "deshacer").
     */
    public void reiniciar() {
        sesion.getCarrito().clear();
        historial.limpiar();
    }

    // ---------------------------------------------------------------- internos

    private void fotografiar() {
        historial.guardarEstado(copiaCarrito());
    }

    private List<CarritoItem> copiaCarrito() {
        List<CarritoItem> copia = new ArrayList<>();
        for (CarritoItem item : sesion.getCarrito()) {
            copia.add(new CarritoItem(item.getProductoId(), item.getCantidad()));
        }
        return copia;
    }

    private void reemplazarCarrito(List<CarritoItem> items) {
        sesion.getCarrito().clear();
        sesion.getCarrito().addAll(items);
    }

    private Optional<CarritoItem> buscarItem(String productoId) {
        return sesion.getCarrito().stream()
                .filter(i -> i.getProductoId().equals(productoId))
                .findFirst();
    }

    private int cantidadDe(String productoId) {
        return buscarItem(productoId).map(CarritoItem::getCantidad).orElse(0);
    }

    private Producto obtenerProducto(String productoId) {
        return repositorioProductos.buscarPorId(productoId)
                .orElseThrow(() -> new ExcepcionNegocio("El producto ya no está disponible"));
    }

    private double redondear(double valor) {
        return Math.round(valor * 100.0) / 100.0;
    }
}
