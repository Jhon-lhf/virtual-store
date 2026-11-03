package com.tienda.servicio;

import com.tienda.modelo.Deseos;
import com.tienda.modelo.Producto;
import com.tienda.persistencia.RepositorioDeseos;
import com.tienda.persistencia.RepositorioProductos;

import java.util.ArrayList;
import java.util.List;

/**
 * Lógica de la lista de deseos: guardar productos para después sin
 * comprometer stock. La lista se persiste con la cuenta del usuario.
 */
public class ServicioDeseos {

    private final RepositorioDeseos repositorioDeseos;
    private final RepositorioProductos repositorioProductos;

    public ServicioDeseos(RepositorioDeseos repositorioDeseos,
                          RepositorioProductos repositorioProductos) {
        this.repositorioDeseos = repositorioDeseos;
        this.repositorioProductos = repositorioProductos;
    }

    /** Agrega un producto a los deseos del usuario (idempotente). */
    public void agregar(String usuarioId, String productoId) {
        repositorioProductos.buscarPorId(productoId)
                .orElseThrow(() -> new ExcepcionNegocio("El producto no existe"));
        Deseos deseos = deseosDe(usuarioId);
        deseos.agregar(productoId);
        repositorioDeseos.guardar(deseos);
    }

    /** Quita un producto de los deseos del usuario. */
    public void quitar(String usuarioId, String productoId) {
        Deseos deseos = deseosDe(usuarioId);
        deseos.quitar(productoId);
        repositorioDeseos.guardar(deseos);
    }

    /** true si el producto está en los deseos del usuario. */
    public boolean esDeseado(String usuarioId, String productoId) {
        return repositorioDeseos.buscarPorUsuario(usuarioId)
                .map(d -> d.contiene(productoId))
                .orElse(false);
    }

    /** Productos guardados por el usuario (los eliminados del catálogo se descartan). */
    public List<Producto> listarProductos(String usuarioId) {
        List<Producto> productos = new ArrayList<>();
        repositorioDeseos.buscarPorUsuario(usuarioId).ifPresent(deseos -> {
            for (String productoId : deseos.getProductoIds()) {
                repositorioProductos.buscarPorId(productoId).ifPresent(productos::add);
            }
        });
        return productos;
    }

    private Deseos deseosDe(String usuarioId) {
        return repositorioDeseos.buscarPorUsuario(usuarioId)
                .orElseGet(() -> new Deseos(usuarioId));
    }
}
