package com.tienda.modelo;

import java.util.ArrayList;
import java.util.List;

/**
 * Producto publicado por un vendedor.
 *
 * <p>La categoría se referencia por {@code categoriaId} (entidad
 * {@link Categoria}); nunca se escribe el nombre a mano, se elige de la
 * lista de categorías aprobadas por administración.</p>
 */
public class Producto {

    /** Umbral a partir del cual un producto se considera con stock bajo. */
    public static final int UMBRAL_STOCK_BAJO = 5;

    private String id;
    private String titulo;
    private String descripcion;
    private double precio;
    private int stock;
    private String categoriaId;
    /** ID del vendedor que publicó el producto. */
    private String vendedorId;
    /** Nombre del archivo dentro de {@code assets/images}; null usa un ícono genérico. */
    private String nombreImagen;
    /** Fecha de publicación en formato ISO (AAAA-MM-DD). */
    private String fechaPublicacion;
    private List<Resena> resenas = new ArrayList<>();

    /** Constructor vacío requerido por Gson. */
    public Producto() {
    }

    public Producto(String id, String titulo, String descripcion, double precio, int stock,
                    String categoriaId, String vendedorId, String fechaPublicacion) {
        this.id = id;
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.precio = precio;
        this.stock = stock;
        this.categoriaId = categoriaId;
        this.vendedorId = vendedorId;
        this.fechaPublicacion = fechaPublicacion;
    }

    /** Promedio de calificaciones de las reseñas; 0 si no hay reseñas. */
    public double calificacionPromedio() {
        if (resenas == null || resenas.isEmpty()) {
            return 0;
        }
        return resenas.stream().mapToInt(Resena::getCalificacion).average().orElse(0);
    }

    public int totalResenas() {
        return resenas == null ? 0 : resenas.size();
    }

    /** true si el stock es positivo pero menor que {@link #UMBRAL_STOCK_BAJO}. */
    public boolean tieneStockBajo() {
        return stock > 0 && stock < UMBRAL_STOCK_BAJO;
    }

    public boolean estaAgotado() {
        return stock <= 0;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public double getPrecio() {
        return precio;
    }

    public void setPrecio(double precio) {
        this.precio = precio;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    public String getCategoriaId() {
        return categoriaId;
    }

    public void setCategoriaId(String categoriaId) {
        this.categoriaId = categoriaId;
    }

    public String getVendedorId() {
        return vendedorId;
    }

    public void setVendedorId(String vendedorId) {
        this.vendedorId = vendedorId;
    }

    public String getNombreImagen() {
        return nombreImagen;
    }

    public void setNombreImagen(String nombreImagen) {
        this.nombreImagen = nombreImagen;
    }

    public String getFechaPublicacion() {
        return fechaPublicacion;
    }

    public void setFechaPublicacion(String fechaPublicacion) {
        this.fechaPublicacion = fechaPublicacion;
    }

    public List<Resena> getResenas() {
        return resenas;
    }

    public void setResenas(List<Resena> resenas) {
        this.resenas = resenas == null ? new ArrayList<>() : resenas;
    }
}
