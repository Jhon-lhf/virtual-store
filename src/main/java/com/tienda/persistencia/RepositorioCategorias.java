package com.tienda.persistencia;

import com.google.gson.reflect.TypeToken;
import com.tienda.modelo.Categoria;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio de categorías sobre {@code data/categorias.json}.
 * Desde el rediseño, la categoría es una entidad propia (no un texto libre
 * dentro del producto).
 */
public class RepositorioCategorias {

    private final AlmacenJson<Categoria> almacen;

    /** Constructor de producción: usa {@code data/categorias.json}. */
    public RepositorioCategorias() {
        this(Paths.get("data", "categorias.json"));
    }

    /** Constructor con ruta explícita (usado por las pruebas con archivos temporales). */
    public RepositorioCategorias(Path archivo) {
        this.almacen = new AlmacenJson<>(archivo, new TypeToken<List<Categoria>>() {
        });
    }

    public List<Categoria> leerTodas() {
        return almacen.leerTodos();
    }

    /** Inserta o actualiza la categoría (buscada por ID) y persiste de inmediato. */
    public synchronized void guardar(Categoria categoria) {
        List<Categoria> categorias = almacen.leerTodos();
        categorias.removeIf(c -> c.getId().equals(categoria.getId()));
        categorias.add(categoria);
        almacen.guardarTodos(categorias);
    }

    public Optional<Categoria> buscarPorId(String id) {
        return almacen.leerTodos().stream()
                .filter(c -> c.getId().equals(id))
                .findFirst();
    }

    /** Búsqueda por nombre sin distinguir mayúsculas/minúsculas ni espacios extremos. */
    public Optional<Categoria> buscarPorNombre(String nombre) {
        String buscado = nombre == null ? "" : nombre.trim();
        return almacen.leerTodos().stream()
                .filter(c -> c.getNombre().equalsIgnoreCase(buscado))
                .findFirst();
    }
}
