package com.tienda.persistencia;

import com.google.gson.reflect.TypeToken;
import com.tienda.modelo.Producto;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio de productos sobre {@code data/productos.json}.
 * El orden del array define el orden de "relevancia" del catálogo.
 */
public class RepositorioProductos {

    private final AlmacenJson<Producto> almacen;

    /** Constructor de producción: usa {@code data/productos.json}. */
    public RepositorioProductos() {
        this(Paths.get("data", "productos.json"));
    }

    /** Constructor con ruta explícita (usado por las pruebas con archivos temporales). */
    public RepositorioProductos(Path archivo) {
        this.almacen = new AlmacenJson<>(archivo, new TypeToken<List<Producto>>() {
        });
    }

    public List<Producto> leerTodos() {
        return almacen.leerTodos();
    }

    /** Inserta o actualiza el producto (buscado por ID) y persiste de inmediato. */
    public synchronized void guardar(Producto producto) {
        List<Producto> productos = almacen.leerTodos();
        productos.removeIf(p -> p.getId().equals(producto.getId()));
        productos.add(producto);
        almacen.guardarTodos(productos);
    }

    public Optional<Producto> buscarPorId(String id) {
        return almacen.leerTodos().stream()
                .filter(p -> p.getId().equals(id))
                .findFirst();
    }

    /** Borrado definitivo (usado por el catálogo global del admin). */
    public synchronized void eliminarPorId(String id) {
        List<Producto> productos = almacen.leerTodos();
        productos.removeIf(p -> p.getId().equals(id));
        almacen.guardarTodos(productos);
    }
}
