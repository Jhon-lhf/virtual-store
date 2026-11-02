package com.tienda.persistencia;

import com.google.gson.reflect.TypeToken;
import com.tienda.modelo.EstadoSolicitud;
import com.tienda.modelo.SolicitudCategoria;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio de solicitudes de categoría sobre {@code data/solicitudes-categoria.json}.
 * Las más recientes van primero (la bandeja del admin muestra lo nuevo arriba).
 */
public class RepositorioSolicitudesCategoria {

    private final AlmacenJson<SolicitudCategoria> almacen;

    /** Constructor de producción: usa {@code data/solicitudes-categoria.json}. */
    public RepositorioSolicitudesCategoria() {
        this(Paths.get("data", "solicitudes-categoria.json"));
    }

    /** Constructor con ruta explícita (usado por las pruebas con archivos temporales). */
    public RepositorioSolicitudesCategoria(Path archivo) {
        this.almacen = new AlmacenJson<>(archivo, new TypeToken<List<SolicitudCategoria>>() {
        });
    }

    /** Todas las solicitudes, las más recientes primero. */
    public List<SolicitudCategoria> leerTodas() {
        List<SolicitudCategoria> solicitudes = almacen.leerTodos();
        solicitudes.sort(Comparator.comparing(SolicitudCategoria::getFecha).reversed());
        return solicitudes;
    }

    /** Inserta o actualiza la solicitud (buscada por ID) y persiste de inmediato. */
    public synchronized void guardar(SolicitudCategoria solicitud) {
        List<SolicitudCategoria> solicitudes = almacen.leerTodos();
        solicitudes.removeIf(s -> s.getId().equals(solicitud.getId()));
        solicitudes.add(solicitud);
        almacen.guardarTodos(solicitudes);
    }

    public Optional<SolicitudCategoria> buscarPorId(String id) {
        return almacen.leerTodos().stream()
                .filter(s -> s.getId().equals(id))
                .findFirst();
    }

    public List<SolicitudCategoria> buscarPendientes() {
        return leerTodas().stream()
                .filter(s -> s.getEstado() == EstadoSolicitud.PENDIENTE)
                .toList();
    }
}
