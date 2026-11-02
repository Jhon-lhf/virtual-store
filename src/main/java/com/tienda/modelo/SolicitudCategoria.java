package com.tienda.modelo;

/**
 * Petición de un vendedor para crear una categoría nueva que no existe aún.
 *
 * <p>Flujo: el vendedor la envía desde el formulario de nuevo producto →
 * queda {@code PENDIENTE} → un administrador la aprueba (se crea la
 * {@link Categoria} y queda disponible para todos) o la rechaza.</p>
 */
public class SolicitudCategoria {

    private String id;
    /** Nombre de categoría propuesto. */
    private String nombre;
    /** Explicación del vendedor sobre por qué necesita esta categoría. */
    private String justificacion;
    private String solicitanteId;
    /** Fecha de envío en formato ISO (AAAA-MM-DD). */
    private String fecha;
    private EstadoSolicitud estado;
    /** Fecha de resolución (aprobación o rechazo); null mientras esté pendiente. */
    private String fechaResolucion;

    /** Constructor vacío requerido por Gson. */
    public SolicitudCategoria() {
    }

    public SolicitudCategoria(String id, String nombre, String justificacion,
                              String solicitanteId, String fecha, EstadoSolicitud estado) {
        this.id = id;
        this.nombre = nombre;
        this.justificacion = justificacion;
        this.solicitanteId = solicitanteId;
        this.fecha = fecha;
        this.estado = estado;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getJustificacion() {
        return justificacion;
    }

    public void setJustificacion(String justificacion) {
        this.justificacion = justificacion;
    }

    public String getSolicitanteId() {
        return solicitanteId;
    }

    public void setSolicitanteId(String solicitanteId) {
        this.solicitanteId = solicitanteId;
    }

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    public EstadoSolicitud getEstado() {
        return estado;
    }

    public void setEstado(EstadoSolicitud estado) {
        this.estado = estado;
    }

    public String getFechaResolucion() {
        return fechaResolucion;
    }

    public void setFechaResolucion(String fechaResolucion) {
        this.fechaResolucion = fechaResolucion;
    }
}
