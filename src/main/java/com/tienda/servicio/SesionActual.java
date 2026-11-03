package com.tienda.servicio;

import com.tienda.modelo.CarritoItem;
import com.tienda.modelo.Usuario;

import java.util.ArrayList;
import java.util.List;

/**
 * Contexto de la sesión actual: el usuario que inició sesión y su carrito
 * de compras (el carrito es transitorio: vive mientras la sesión está
 * abierta y se limpia al cerrarla). La interfaz lo consulta para saber qué
 * módulos mostrar y a quién atribuir acciones.
 */
public class SesionActual {

    private Usuario usuario;
    private final List<CarritoItem> carrito = new ArrayList<>();

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public boolean haySesion() {
        return usuario != null;
    }

    public List<CarritoItem> getCarrito() {
        return carrito;
    }

    public void limpiar() {
        usuario = null;
        carrito.clear();
    }
}
