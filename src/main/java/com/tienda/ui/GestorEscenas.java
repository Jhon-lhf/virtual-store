package com.tienda.ui;

import com.tienda.modelo.Usuario;
import com.tienda.servicio.ServicioAuth;
import com.tienda.servicio.ServicioCategorias;
import com.tienda.servicio.ServicioProductos;
import com.tienda.servicio.SesionActual;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Equivalente al "MainFrame": gestiona qué escena se muestra (login o
 * principal), aplica el tema claro/oscuro y expone los servicios a las
 * vistas. Las vistas reciben esta clase para navegar y avisar, igual que
 * los paneles recibían {@code MainFrame} en el diseño Swing.
 */
public class GestorEscenas {

    private static final String CSS_BASE = "/css/estilos.css";
    private static final String CSS_CLARO = "/css/tema-claro.css";
    private static final String CSS_OSCURO = "/css/tema-oscuro.css";

    private final Stage escenario;
    private final ServicioAuth servicioAuth;
    private final ServicioProductos servicioProductos;
    private final ServicioCategorias servicioCategorias;
    private final SesionActual sesion = new SesionActual();
    private boolean temaOscuro = false;

    public GestorEscenas(Stage escenario, ServicioAuth servicioAuth,
                         ServicioProductos servicioProductos, ServicioCategorias servicioCategorias) {
        this.escenario = escenario;
        this.servicioAuth = servicioAuth;
        this.servicioProductos = servicioProductos;
        this.servicioCategorias = servicioCategorias;
    }

    /** Muestra la pantalla de inicio de sesión. */
    public void mostrarLogin() {
        VistaLogin vista = new VistaLogin(this);
        Scene escena = new Scene(vista.getRaiz());
        aplicarEstilos(escena);
        escenario.setScene(escena);
        escenario.setTitle("Tienda Virtual — Iniciar sesión");
    }

    /** Muestra el shell principal con las pestañas del rol en sesión. */
    public void mostrarPrincipal() {
        VistaPrincipal vista = new VistaPrincipal(this);
        Scene escena = new Scene(vista.getRaiz());
        aplicarEstilos(escena);
        escenario.setScene(escena);
        Usuario usuario = sesion.getUsuario();
        escenario.setTitle("Tienda Virtual — " + usuario.getNombre());
    }

    /** Cierra la sesión actual y vuelve al login. */
    public void cerrarSesion() {
        sesion.limpiar();
        mostrarLogin();
    }

    /** Alterna entre tema claro y oscuro; se aplica a la escena visible al instante. */
    public void alternarTema() {
        temaOscuro = !temaOscuro;
        Scene escena = escenario.getScene();
        if (escena != null) {
            aplicarEstilos(escena);
        }
    }

    /** Aplica los estilos base más el tema actual (también a ventanas modales). */
    public void aplicarEstilos(Scene escena) {
        escena.getStylesheets().setAll(CSS_BASE, temaOscuro ? CSS_OSCURO : CSS_CLARO);
    }

    public boolean isTemaOscuro() {
        return temaOscuro;
    }

    public Stage getEscenario() {
        return escenario;
    }

    public ServicioAuth getServicioAuth() {
        return servicioAuth;
    }

    public ServicioProductos getServicioProductos() {
        return servicioProductos;
    }

    public ServicioCategorias getServicioCategorias() {
        return servicioCategorias;
    }

    public SesionActual getSesion() {
        return sesion;
    }
}
