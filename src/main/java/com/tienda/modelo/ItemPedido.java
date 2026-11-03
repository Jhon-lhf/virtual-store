package com.tienda.modelo;

/**
 * Línea de un pedido: fotografía del producto al momento de la compra
 * (título, precio unitario y vendedor), de modo que el pedido conserva su
 * sentido histórico aunque el producto cambie de precio o se elimine después.
 */
public class ItemPedido {

    private String productoId;
    private String titulo;
    private String vendedorId;
    private double precioUnitario;
    private int cantidad;

    /** Constructor vacío requerido por Gson. */
    public ItemPedido() {
    }

    public ItemPedido(String productoId, String titulo, String vendedorId,
                      double precioUnitario, int cantidad) {
        this.productoId = productoId;
        this.titulo = titulo;
        this.vendedorId = vendedorId;
        this.precioUnitario = precioUnitario;
        this.cantidad = cantidad;
    }

    public double subtotal() {
        return precioUnitario * cantidad;
    }

    public String getProductoId() {
        return productoId;
    }

    public void setProductoId(String productoId) {
        this.productoId = productoId;
    }

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getVendedorId() {
        return vendedorId;
    }

    public void setVendedorId(String vendedorId) {
        this.vendedorId = vendedorId;
    }

    public double getPrecioUnitario() {
        return precioUnitario;
    }

    public void setPrecioUnitario(double precioUnitario) {
        this.precioUnitario = precioUnitario;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }
}
