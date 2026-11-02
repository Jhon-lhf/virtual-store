package com.tienda.persistencia;

import com.google.gson.reflect.TypeToken;
import com.tienda.modelo.Usuario;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio de usuarios sobre {@code data/usuarios.json}.
 * Único punto que toca el archivo; los servicios nunca leen el disco directo.
 */
public class RepositorioUsuarios {

    private final AlmacenJson<Usuario> almacen;

    /** Constructor de producción: usa {@code data/usuarios.json}. */
    public RepositorioUsuarios() {
        this(Paths.get("data", "usuarios.json"));
    }

    /** Constructor con ruta explícita (usado por las pruebas con archivos temporales). */
    public RepositorioUsuarios(Path archivo) {
        this.almacen = new AlmacenJson<>(archivo, new TypeToken<List<Usuario>>() {
        });
    }

    public List<Usuario> leerTodos() {
        return almacen.leerTodos();
    }

    /** Inserta o actualiza el usuario (buscado por ID) y persiste de inmediato. */
    public synchronized void guardar(Usuario usuario) {
        List<Usuario> usuarios = almacen.leerTodos();
        usuarios.removeIf(u -> u.getId().equals(usuario.getId()));
        usuarios.add(usuario);
        almacen.guardarTodos(usuarios);
    }

    public Optional<Usuario> buscarPorId(String id) {
        return almacen.leerTodos().stream()
                .filter(u -> u.getId().equals(id))
                .findFirst();
    }

    /** Búsqueda por nombre de usuario sin distinguir mayúsculas/minúsculas. */
    public Optional<Usuario> buscarPorNombreUsuario(String usuario) {
        String buscado = usuario == null ? "" : usuario.trim();
        return almacen.leerTodos().stream()
                .filter(u -> u.getUsuario().equalsIgnoreCase(buscado))
                .findFirst();
    }
}
