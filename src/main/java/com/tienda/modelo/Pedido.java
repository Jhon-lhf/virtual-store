package com.tienda.modelo;

import java.util.ArrayList;
import java.util.List;

/**
 * Pedido de un cliente: snapshot de los ítems comprados con sus totales
 * (subtotal, impuesto del 19% y total) y el estado del despacho.
 */
public class Pedido {

    private String id;
    private String clienteId;
    private String clienteNombre;
    /** Fecha de creación en formato ISO (AAAA-MM-DD). */
    private String fecha;
    private EstadoPedido estado;
    private List<ItemPedido> items = new ArrayList<>();
    private double subtotal;
    private double impuesto;
    private double total;

    /** Constructor vacío requerido por Gson. */
    public Pedido() {
    }

    public Pedido(String id, String clienteId, String clienteNombre, String fecha,
                  EstadoPedido estado, List<ItemPedido> items,
                  double subtotal, double impuesto, double total) {
        this.id = id;
        this.clienteId = clienteId;
        this.clienteNombre = clienteNombre;
        this.fecha = fecha;
        this.estado = estado;
        this.items = items == null ? new ArrayList<>() : items;
        this.subtotal = subtotal;
        this.impuesto = impuesto;
        this.total = total;
    }

    /** Cantidad total de unidades del pedido (para resúmenes). */
    public int totalUnidades() {
        return items.stream().mapToInt(ItemPedido::getCantidad).sum();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getClienteId() {
        return clienteId;
    }

    public void setClienteId(String clienteId) {
        this.clienteId = clienteId;
    }

    public String getClienteNombre() {
        return clienteNombre;
    }

    public void setClienteNombre(String clienteNombre) {
        this.clienteNombre = clienteNombre;
    }

    public String getFecha() {
        return fecha;
    }

    public void setFecha(String fecha) {
        this.fecha = fecha;
    }

    public EstadoPedido getEstado() {
        return estado;
    }

    public void setEstado(EstadoPedido estado) {
        this.estado = estado;
    }

    public List<ItemPedido> getItems() {
        return items;
    }

    public void setItems(List<ItemPedido> items) {
        this.items = items == null ? new ArrayList<>() : items;
    }

    public double getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(double subtotal) {
        this.subtotal = subtotal;
    }

    public double getImpuesto() {
        return impuesto;
    }

    public void setImpuesto(double impuesto) {
        this.impuesto = impuesto;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }
}
