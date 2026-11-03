package com.tienda.persistencia;

import com.google.gson.reflect.TypeToken;
import com.tienda.modelo.Deseos;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio de listas de deseos sobre {@code data/deseos.json}.
 * Un registro {@link Deseos} por usuario.
 */
public class RepositorioDeseos {

    private final AlmacenJson<Deseos> almacen;

    /** Constructor de producción: usa {@code data/deseos.json}. */
    public RepositorioDeseos() {
        this(Paths.get("data", "deseos.json"));
    }

    /** Constructor con ruta explícita (usado por las pruebas con archivos temporales). */
    public RepositorioDeseos(Path archivo) {
        this.almacen = new AlmacenJson<>(archivo, new TypeToken<List<Deseos>>() {
        });
    }

    public List<Deseos> leerTodos() {
        return almacen.leerTodos();
    }

    public Optional<Deseos> buscarPorUsuario(String usuarioId) {
        return almacen.leerTodos().stream()
                .filter(d -> d.getUsuarioId().equals(usuarioId))
                .findFirst();
    }

    /** Inserta o actualiza la lista del usuario y persiste de inmediato. */
    public synchronized void guardar(Deseos deseos) {
        List<Deseos> todos = almacen.leerTodos();
        todos.removeIf(d -> d.getUsuarioId().equals(deseos.getUsuarioId()));
        todos.add(deseos);
        almacen.guardarTodos(todos);
    }
}
