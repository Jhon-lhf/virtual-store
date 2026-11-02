package com.tienda.servicio;

import com.tienda.modelo.Usuario;

/**
 * Contexto de la sesión actual: el usuario que inició sesión.
 * La interfaz lo consulta para saber qué pestañas mostrar y a quién
 * atribuir acciones; se limpia al cerrar sesión.
 */
public class SesionActual {

    private Usuario usuario;

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public boolean haySesion() {
        return usuario != null;
    }

    public void limpiar() {
        usuario = null;
    }
}
