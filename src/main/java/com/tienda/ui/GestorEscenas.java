package com.tienda.ui;

import com.tienda.modelo.Usuario;
import com.tienda.servicio.ServicioAuth;
import com.tienda.servicio.ServicioCategorias;
import com.tienda.servicio.ServicioCarrito;
import com.tienda.servicio.ServicioDeseos;
import com.tienda.servicio.ServicioPedidos;
import com.tienda.servicio.ServicioProductos;
import com.tienda.servicio.ServicioResenas;
import com.tienda.servicio.SesionActual;
import javafx.scene.Scene;
import javafx.stage.Screen;
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
    private final SesionActual sesion;
    private final ServicioAuth servicioAuth;
    private final ServicioProductos servicioProductos;
    private final ServicioCategorias servicioCategorias;
    private final ServicioCarrito servicioCarrito;
    private final ServicioPedidos servicioPedidos;
    private final ServicioResenas servicioResenas;
    private final ServicioDeseos servicioDeseos;
    private boolean temaOscuro = false;

    public GestorEscenas(Stage escenario, SesionActual sesion,
                         ServicioAuth servicioAuth,
                         ServicioProductos servicioProductos,
                         ServicioCategorias servicioCategorias,
                         ServicioCarrito servicioCarrito,
                         ServicioPedidos servicioPedidos,
                         ServicioResenas servicioResenas,
                         ServicioDeseos servicioDeseos) {
        this.escenario = escenario;
        this.sesion = sesion;
        this.servicioAuth = servicioAuth;
        this.servicioProductos = servicioProductos;
        this.servicioCategorias = servicioCategorias;
        this.servicioCarrito = servicioCarrito;
        this.servicioPedidos = servicioPedidos;
        this.servicioResenas = servicioResenas;
        this.servicioDeseos = servicioDeseos;
    }

    /** Muestra la pantalla de inicio de sesión, en una ventana compacta no redimensionable. */
    public void mostrarLogin() {
        VistaLogin vista = new VistaLogin(this);
        Scene escena = new Scene(vista.getRaiz());
        aplicarEstilos(escena);
        escenario.setScene(escena);
        escenario.setTitle("Tienda Virtual — Iniciar sesión");
        // El shell principal deja mínimos de 960x640; hay que liberarlos para la ventana compacta.
        escenario.setMinWidth(0);
        escenario.setMinHeight(0);
        ajustarVentana(480, 700, false);
    }

    /** Muestra el shell principal con la navegación lateral del rol en sesión. */
    public void mostrarPrincipal() {
        // Sesión nueva: carrito e historial de deshacer parten de cero.
        servicioCarrito.reiniciar();
        VistaPrincipal vista = new VistaPrincipal(this);
        Scene escena = new Scene(vista.getRaiz());
        aplicarEstilos(escena);
        escenario.setScene(escena);
        Usuario usuario = sesion.getUsuario();
        escenario.setTitle("Tienda Virtual — " + usuario.getNombre());
        escenario.setMinWidth(960);
        escenario.setMinHeight(640);
        ajustarVentana(1280, 820, true);
    }

    /**
     * Dimensiona la ventana sin desbordar la pantalla: en monitores pequeños
     * (por ejemplo 1366x768) una ventana fija de 1280x820 queda cortada por
     * la barra de tareas, así que se limita al 90% del área visible.
     */
    private void ajustarVentana(double ancho, double alto, boolean redimensionable) {
        var limites = Screen.getPrimary().getVisualBounds();
        escenario.setResizable(redimensionable);
        escenario.setWidth(Math.min(ancho, limites.getWidth() * 0.9));
        escenario.setHeight(Math.min(alto, limites.getHeight() * 0.9));
        escenario.centerOnScreen();
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

    public ServicioCarrito getServicioCarrito() {
        return servicioCarrito;
    }

    public ServicioPedidos getServicioPedidos() {
        return servicioPedidos;
    }

    public ServicioResenas getServicioResenas() {
        return servicioResenas;
    }

    public ServicioDeseos getServicioDeseos() {
        return servicioDeseos;
    }

    public SesionActual getSesion() {
        return sesion;
    }
}
