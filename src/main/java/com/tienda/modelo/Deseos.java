package com.tienda.modelo;

import java.util.ArrayList;
import java.util.List;

/**
 * Lista de deseos de un usuario: productos guardados para después, sin
 * comprometer stock. Se persiste con la cuenta (un registro por usuario,
 * buscado por {@code usuarioId}).
 */
public class Deseos {

    private String usuarioId;
    private List<String> productoIds = new ArrayList<>();

    /** Constructor vacío requerido por Gson. */
    public Deseos() {
    }

    public Deseos(String usuarioId) {
        this.usuarioId = usuarioId;
    }

    public boolean contiene(String productoId) {
        return productoIds.contains(productoId);
    }

    public void agregar(String productoId) {
        if (!contiene(productoId)) {
            productoIds.add(productoId);
        }
    }

    public void quitar(String productoId) {
        productoIds.remove(productoId);
    }

    public String getUsuarioId() {
        return usuarioId;
    }

    public void setUsuarioId(String usuarioId) {
        this.usuarioId = usuarioId;
    }

    public List<String> getProductoIds() {
        return productoIds;
    }

    public void setProductoIds(List<String> productoIds) {
        this.productoIds = productoIds == null ? new ArrayList<>() : productoIds;
    }
}
