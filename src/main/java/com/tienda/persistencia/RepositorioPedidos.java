package com.tienda.persistencia;

import com.google.gson.reflect.TypeToken;
import com.tienda.modelo.Pedido;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio de pedidos sobre {@code data/pedidos.json}.
 */
public class RepositorioPedidos {

    private final AlmacenJson<Pedido> almacen;

    /** Constructor de producción: usa {@code data/pedidos.json}. */
    public RepositorioPedidos() {
        this(Paths.get("data", "pedidos.json"));
    }

    /** Constructor con ruta explícita (usado por las pruebas con archivos temporales). */
    public RepositorioPedidos(Path archivo) {
        this.almacen = new AlmacenJson<>(archivo, new TypeToken<List<Pedido>>() {
        });
    }

    public List<Pedido> leerTodos() {
        return almacen.leerTodos();
    }

    /** Inserta o actualiza el pedido (buscado por ID) y persiste de inmediato. */
    public synchronized void guardar(Pedido pedido) {
        List<Pedido> pedidos = almacen.leerTodos();
        pedidos.removeIf(p -> p.getId().equals(pedido.getId()));
        pedidos.add(pedido);
        almacen.guardarTodos(pedidos);
    }

    public Optional<Pedido> buscarPorId(String id) {
        return almacen.leerTodos().stream()
                .filter(p -> p.getId().equals(id))
                .findFirst();
    }
}
