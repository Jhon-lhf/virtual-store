package com.tienda.servicio;

import com.tienda.modelo.Categoria;
import com.tienda.modelo.EstadoSolicitud;
import com.tienda.modelo.OrigenCategoria;
import com.tienda.modelo.Producto;
import com.tienda.modelo.SolicitudCategoria;
import com.tienda.modelo.Usuario;
import com.tienda.persistencia.RepositorioCategorias;
import com.tienda.persistencia.RepositorioProductos;
import com.tienda.persistencia.RepositorioSolicitudesCategoria;
import com.tienda.util.GeneradorId;

import java.time.LocalDate;
import java.util.List;

/**
 * Gobierno de las categorías del sistema.
 *
 * <p>Reglas clave del rediseño:</p>
 * <ul>
 *   <li>El formulario de publicación solo ofrece categorías existentes
 *       (aprobadas): se acabaron los duplicados y errores de tipeo.</li>
 *   <li>Un vendedor puede <b>solicitar</b> una categoría nueva con
 *       justificación; un administrador la aprueba (se crea la categoría y
 *       queda disponible) o la rechaza.</li>
 *   <li>El administrador también puede crear categorías directamente.</li>
 *   <li>Una categoría sin productos no aparece en el catálogo del cliente
 *       (el combo se deriva de los productos publicados).</li>
 * </ul>
 */
public class ServicioCategorias {

    private final RepositorioCategorias repositorioCategorias;
    private final RepositorioSolicitudesCategoria repositorioSolicitudes;
    private final RepositorioProductos repositorioProductos;

    public ServicioCategorias(RepositorioCategorias repositorioCategorias,
                              RepositorioSolicitudesCategoria repositorioSolicitudes,
                              RepositorioProductos repositorioProductos) {
        this.repositorioCategorias = repositorioCategorias;
        this.repositorioSolicitudes = repositorioSolicitudes;
        this.repositorioProductos = repositorioProductos;
    }

    /** Todas las categorías del sistema (las creadas son activas por definición). */
    public List<Categoria> listarActivas() {
        return repositorioCategorias.leerTodas();
    }

    /**
     * Categorías que el cliente debe ver en el combo del catálogo: solo las
     * que tienen al menos un producto publicado.
     */
    public List<Categoria> listarActivasConProductos() {
        List<String> idsEnUso = repositorioProductos.leerTodos().stream()
                .map(Producto::getCategoriaId)
                .distinct()
                .toList();
        return repositorioCategorias.leerTodas().stream()
                .filter(c -> idsEnUso.contains(c.getId()))
                .toList();
    }

    /** Categorías que el vendedor puede elegir al publicar: todas las existentes. */
    public List<Categoria> listarParaPublicar() {
        return repositorioCategorias.leerTodas();
    }

    /**
     * El vendedor solicita una categoría nueva con justificación.
     *
     * @throws ExcepcionNegocio si falta el nombre, la justificación o si ya
     *                          existe la categoría o hay una solicitud pendiente con ese nombre.
     */
    public SolicitudCategoria solicitar(String nombre, String justificacion, Usuario solicitante) {
        String nombreLimpio = nombre == null ? "" : nombre.trim();
        if (nombreLimpio.isEmpty()) {
            throw new ExcepcionNegocio("Escribe el nombre de la categoría");
        }
        if (justificacion == null || justificacion.isBlank()) {
            throw new ExcepcionNegocio("Explica por qué necesitas esta categoría");
        }
        validarNombreDisponible(nombreLimpio);
        boolean pendienteIgual = repositorioSolicitudes.buscarPendientes().stream()
                .anyMatch(s -> s.getNombre().equalsIgnoreCase(nombreLimpio));
        if (pendienteIgual) {
            throw new ExcepcionNegocio("Ya existe una solicitud pendiente para \"" + nombreLimpio + "\"");
        }

        SolicitudCategoria solicitud = new SolicitudCategoria(
                GeneradorId.generar("S"),
                nombreLimpio,
                justificacion.trim(),
                solicitante.getId(),
                LocalDate.now().toString(),
                EstadoSolicitud.PENDIENTE);
        repositorioSolicitudes.guardar(solicitud);
        return solicitud;
    }

    /** Todas las solicitudes (para la bandeja del admin), recientes primero. */
    public List<SolicitudCategoria> listarSolicitudes() {
        return repositorioSolicitudes.leerTodas();
    }

    public List<SolicitudCategoria> listarPendientes() {
        return repositorioSolicitudes.buscarPendientes();
    }

    /**
     * Aprueba una solicitud pendiente: crea la categoría y queda disponible
     * de inmediato para publicar.
     *
     * @throws ExcepcionNegocio si la solicitud no está pendiente o el nombre
     *                          ya fue ocupado mientras tanto.
     */
    public Categoria aprobar(String idSolicitud) {
        SolicitudCategoria solicitud = repositorioSolicitudes.buscarPorId(idSolicitud)
                .orElseThrow(() -> new ExcepcionNegocio("La solicitud no existe"));
        if (solicitud.getEstado() != EstadoSolicitud.PENDIENTE) {
            throw new ExcepcionNegocio("La solicitud ya fue resuelta");
        }
        validarNombreDisponible(solicitud.getNombre());

        Categoria categoria = new Categoria(
                GeneradorId.generar("C"),
                solicitud.getNombre(),
                LocalDate.now().toString(),
                OrigenCategoria.SOLICITUD);
        repositorioCategorias.guardar(categoria);

        solicitud.setEstado(EstadoSolicitud.APROBADA);
        solicitud.setFechaResolucion(LocalDate.now().toString());
        repositorioSolicitudes.guardar(solicitud);
        return categoria;
    }

    /**
     * Rechaza una solicitud pendiente: no crea ninguna categoría.
     *
     * @throws ExcepcionNegocio si la solicitud no está pendiente.
     */
    public void rechazar(String idSolicitud) {
        SolicitudCategoria solicitud = repositorioSolicitudes.buscarPorId(idSolicitud)
                .orElseThrow(() -> new ExcepcionNegocio("La solicitud no existe"));
        if (solicitud.getEstado() != EstadoSolicitud.PENDIENTE) {
            throw new ExcepcionNegocio("La solicitud ya fue resuelta");
        }
        solicitud.setEstado(EstadoSolicitud.RECHAZADA);
        solicitud.setFechaResolucion(LocalDate.now().toString());
        repositorioSolicitudes.guardar(solicitud);
    }

    /** Creación directa por el administrador (sin solicitud previa). */
    public Categoria crearDirecta(String nombre) {
        String nombreLimpio = nombre == null ? "" : nombre.trim();
        if (nombreLimpio.isEmpty()) {
            throw new ExcepcionNegocio("Escribe el nombre de la categoría");
        }
        validarNombreDisponible(nombreLimpio);
        Categoria categoria = new Categoria(
                GeneradorId.generar("C"),
                nombreLimpio,
                LocalDate.now().toString(),
                OrigenCategoria.ADMINISTRADOR);
        repositorioCategorias.guardar(categoria);
        return categoria;
    }

    /** Cantidad de productos publicados en una categoría (para la tabla del admin). */
    public long contarProductos(String categoriaId) {
        return repositorioProductos.leerTodos().stream()
                .filter(p -> p.getCategoriaId().equals(categoriaId))
                .count();
    }

    /** El nombre no debe existir aún (comparación sin mayúsculas). */
    private void validarNombreDisponible(String nombre) {
        if (repositorioCategorias.buscarPorNombre(nombre).isPresent()) {
            throw new ExcepcionNegocio("La categoría \"" + nombre + "\" ya existe");
        }
    }
}
