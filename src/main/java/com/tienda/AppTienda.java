package com.tienda;

import com.tienda.persistencia.RepositorioCategorias;
import com.tienda.persistencia.RepositorioDeseos;
import com.tienda.persistencia.RepositorioPedidos;
import com.tienda.persistencia.RepositorioProductos;
import com.tienda.persistencia.RepositorioSolicitudesCategoria;
import com.tienda.persistencia.RepositorioUsuarios;
import com.tienda.servicio.ServicioAuth;
import com.tienda.servicio.ServicioCategorias;
import com.tienda.servicio.ServicioCarrito;
import com.tienda.servicio.ServicioDeseos;
import com.tienda.servicio.ServicioPedidos;
import com.tienda.servicio.ServicioProductos;
import com.tienda.servicio.ServicioResenas;
import com.tienda.servicio.SesionActual;
import com.tienda.ui.GestorEscenas;
import javafx.application.Application;
import javafx.stage.Stage;

/**
 * Punto de entrada JavaFX: construye la capa de servicios (con sus
 * repositorios), siembra los datos demo si hace falta y entrega el control
 * al {@link GestorEscenas}, que gestiona la navegación y el tema.
 */
public class AppTienda extends Application {

    @Override
    public void start(Stage escenario) {
        RepositorioUsuarios repoUsuarios = new RepositorioUsuarios();
        RepositorioProductos repoProductos = new RepositorioProductos();
        RepositorioCategorias repoCategorias = new RepositorioCategorias();
        RepositorioSolicitudesCategoria repoSolicitudes = new RepositorioSolicitudesCategoria();
        RepositorioPedidos repoPedidos = new RepositorioPedidos();
        RepositorioDeseos repoDeseos = new RepositorioDeseos();

        Semilla.ejecutarSiNecesario(repoUsuarios, repoProductos, repoCategorias, repoDeseos);

        // La sesión se crea aquí para que el carrito (que vive en ella) y el
        // gestor de escenas compartan el mismo contexto.
        SesionActual sesion = new SesionActual();
        ServicioCarrito servicioCarrito = new ServicioCarrito(repoProductos, sesion);

        GestorEscenas app = new GestorEscenas(
                escenario,
                sesion,
                new ServicioAuth(repoUsuarios),
                new ServicioProductos(repoProductos, repoCategorias),
                new ServicioCategorias(repoCategorias, repoSolicitudes, repoProductos),
                servicioCarrito,
                new ServicioPedidos(repoPedidos, repoProductos),
                new ServicioResenas(repoProductos),
                new ServicioDeseos(repoDeseos, repoProductos));

        escenario.setTitle("Tienda Virtual");

        app.mostrarLogin();
        escenario.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
