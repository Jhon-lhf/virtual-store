package com.tienda.modelo;

/**
 * Línea del carrito de compras: referencia a un producto y la cantidad
 * deseada. Es un dato de sesión (no se persiste); el precio siempre se lee
 * del producto vigente para que los totales reflejen el valor actual.
 */
public class CarritoItem {

    private String productoId;
    private int cantidad;

    /** Constructor vacío (consistente con los demás POJOs del modelo). */
    public CarritoItem() {
    }

    public CarritoItem(String productoId, int cantidad) {
        this.productoId = productoId;
        this.cantidad = cantidad;
    }

    public String getProductoId() {
        return productoId;
    }

    public void setProductoId(String productoId) {
        this.productoId = productoId;
    }

    public int getCantidad() {
        return cantidad;
    }

    public void setCantidad(int cantidad) {
        this.cantidad = cantidad;
    }
}
