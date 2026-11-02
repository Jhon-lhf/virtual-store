package com.tienda;

import com.tienda.persistencia.RepositorioCategorias;
import com.tienda.persistencia.RepositorioProductos;
import com.tienda.persistencia.RepositorioSolicitudesCategoria;
import com.tienda.persistencia.RepositorioUsuarios;
import com.tienda.servicio.ServicioAuth;
import com.tienda.servicio.ServicioCategorias;
import com.tienda.servicio.ServicioProductos;
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

        Semilla.ejecutarSiNecesario(repoUsuarios, repoProductos, repoCategorias);

        GestorEscenas app = new GestorEscenas(
                escenario,
                new ServicioAuth(repoUsuarios),
                new ServicioProductos(repoProductos, repoCategorias),
                new ServicioCategorias(repoCategorias, repoSolicitudes, repoProductos));

        escenario.setTitle("Tienda Virtual");

        app.mostrarLogin();
        escenario.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
