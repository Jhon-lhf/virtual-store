package com.tienda.modelo;

/**
 * Categoría de producto como entidad propia (a diferencia del diseño anterior,
 * donde la categoría era un texto libre dentro de {@link Producto}).
 *
 * <p>Las categorías son la única vía de clasificación admitida: el formulario
 * de publicación solo permite elegir categorías existentes, y crear una nueva
 * exige una solicitud aprobada por administración (o creación directa del
 * admin). Esto elimina duplicados y errores de tipeo.</p>
 */
public class Categoria {

    private String id;
    private String nombre;
    /** Fecha de creación en formato ISO (AAAA-MM-DD). */
    private String fechaCreacion;
    private OrigenCategoria origen;

    /** Constructor vacío requerido por Gson. */
    public Categoria() {
    }

    public Categoria(String id, String nombre, String fechaCreacion, OrigenCategoria origen) {
        this.id = id;
        this.nombre = nombre;
        this.fechaCreacion = fechaCreacion;
        this.origen = origen;
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

    public String getFechaCreacion() {
        return fechaCreacion;
    }

    public void setFechaCreacion(String fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }

    public OrigenCategoria getOrigen() {
        return origen;
    }

    public void setOrigen(OrigenCategoria origen) {
        this.origen = origen;
    }
}
