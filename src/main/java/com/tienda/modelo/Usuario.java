package com.tienda.modelo;

import java.util.ArrayList;
import java.util.List;

/**
 * Cuenta de usuario del sistema (cliente, vendedor o administrador).
 *
 * <p>La clave nunca se guarda en texto plano: {@code hashClave} contiene el
 * hash SHA-256 calculado por {@code HashClave} en el momento del registro.</p>
 */
public class Usuario {

    private String id;
    private String usuario;
    private String hashClave;
    private String nombre;
    private String correo;
    private Rol rol;
    private boolean activo;

    /** IDs de productos guardados en la lista de deseos (persisten con la cuenta). */
    private List<String> deseos = new ArrayList<>();

    /** Constructor vacío requerido por Gson. */
    public Usuario() {
    }

    public Usuario(String id, String usuario, String hashClave, String nombre,
                   String correo, Rol rol, boolean activo) {
        this.id = id;
        this.usuario = usuario;
        this.hashClave = hashClave;
        this.nombre = nombre;
        this.correo = correo;
        this.rol = rol;
        this.activo = activo;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public String getHashClave() {
        return hashClave;
    }

    public void setHashClave(String hashClave) {
        this.hashClave = hashClave;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getCorreo() {
        return correo;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public Rol getRol() {
        return rol;
    }

    public void setRol(Rol rol) {
        this.rol = rol;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    public List<String> getDeseos() {
        return deseos;
    }

    public void setDeseos(List<String> deseos) {
        this.deseos = deseos == null ? new ArrayList<>() : deseos;
    }
}
