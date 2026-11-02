package com.tienda.modelo;

/**
 * Reseña que un cliente deja sobre un producto: calificación de 1 a 5
 * estrellas más un comentario. Se guarda embebida dentro del propio
 * {@link Producto}; la calificación mostrada es el promedio.
 */
public class Resena {

    private String autorId;
    private String autorNombre;
    /** Calificación de 1 a 5 estrellas. */
    private int calificacion;
    private String comentario;
    /** Fecha de publicación en formato ISO (AAAA-MM-DD). */
    private String fecha;

    /** Constructor vacío requerido por Gson. */
    public Resena() {
    }

    public Resena(String autorId, String autorNombre, int calificacion, String comentario, String fecha) {
        this.autorId = autorId;
        this.autorNombre = autorNombre;
        this.calificacion = calificacion;
        this.comentario = comentario;
        this.fecha = fecha;
    }

    public String getAutorId() {
        return autorId;
    }

    public void setAutorId(String autorId) {
        this.autorId = autorId;
    }

    public String getAutorNombre() {
        return autorNombre;
    }

    public void setAutorNombre(String autorNombre) {
        this.autorNombre = autorNombre;
    }

    public int getCalificacion() {
        return calificacion;
    }

    public void setCalificacion(int calificacion) {
        this.calificacion = calificacion;
    }

    public String getComentario() {
        return comentario;
    }

    public void setComentario(String comentario) {
        this.comentario = comentario;
    }

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }
}
