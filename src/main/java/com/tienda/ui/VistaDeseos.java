package com.tienda.ui;

import com.tienda.modelo.Producto;
import com.tienda.servicio.ExcepcionNegocio;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.Locale;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Lista de deseos del cliente: productos guardados para después, con
 * acciones para ver el detalle, moverlos al carrito o quitarlos. La lista
 * se persiste con la cuenta (RepositorioDeseos).
 */
public class VistaDeseos {

    private final GestorEscenas app;
    private final BiConsumer<Avisos.Tipo, String> avisador;
    private final Runnable alCambiarCarrito;
    private final Consumer<String> alAbrirDetalle;

    private final VBox raiz = new VBox(14);
    private final VBox listaDeseos = new VBox(10);
    private final Label etContador = new Label();

    public VistaDeseos(GestorEscenas app, BiConsumer<Avisos.Tipo, String> avisador,
                       Runnable alCambiarCarrito, Consumer<String> alAbrirDetalle) {
        this.app = app;
        this.avisador = avisador;
        this.alCambiarCarrito = alCambiarCarrito;
        this.alAbrirDetalle = alAbrirDetalle;
        construir();
        refrescar();
    }

    public Node getRaiz() {
        return raiz;
    }

    private void construir() {
        raiz.setPadding(new Insets(18, 24, 20, 24));

        Label titulo = new Label("Tus deseos");
        titulo.getStyleClass().add("titulo-panel");
        etContador.getStyleClass().add("texto-apagado");
        HBox cabecera = new HBox(10, titulo, etContador);
        cabecera.setAlignment(Pos.CENTER_LEFT);

        ScrollPane desplazamiento = new ScrollPane(listaDeseos);
        desplazamiento.setFitToWidth(true);
        desplazamiento.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        desplazamiento.getStyleClass().add("zona-scroll");
        VBox.setVgrow(desplazamiento, Priority.ALWAYS);

        raiz.getChildren().addAll(cabecera, desplazamiento);
    }

    private void refrescar() {
        List<Producto> productos = app.getServicioDeseos().listarProductos(idSesion());
        etContador.setText(productos.size()
                + (productos.size() == 1 ? " producto guardado" : " productos guardados"));

        listaDeseos.getChildren().clear();
        if (productos.isEmpty()) {
            listaDeseos.getChildren().add(estadoVacio());
            return;
        }
        for (Producto producto : productos) {
            listaDeseos.getChildren().add(filaDeseo(producto));
        }
    }

    private Node estadoVacio() {
        VBox tarjeta = new VBox(10);
        tarjeta.getStyleClass().addAll("panel", "estado-vacio");
        tarjeta.setAlignment(Pos.CENTER);
        tarjeta.setPadding(new Insets(48));
        Icono icono = new Icono(Iconos.DESEOS_CONTORNO, 36);
        icono.getStyleClass().add("icono-modulo");
        Label titulo = new Label("Aún no guardas productos");
        titulo.getStyleClass().add("titulo-modulo");
        Label pista = new Label("Toca el corazón en el detalle de un producto para guardarlo aquí");
        pista.getStyleClass().add("texto-apagado");
        tarjeta.getChildren().addAll(icono, titulo, pista);
        return tarjeta;
    }

    private Node filaDeseo(Producto producto) {
        String nombreCategoria = app.getServicioProductos().nombreCategoria(producto.getCategoriaId());

        StackPane miniImagen = new StackPane();
        miniImagen.getStyleClass().add("mini-imagen");
        miniImagen.setStyle("-fx-background-color: " + VistaTarjetaProducto.gradienteDe(nombreCategoria) + ";");
        Icono icono = new Icono(VistaTarjetaProducto.iconoDe(nombreCategoria), 24);
        icono.getStyleClass().add("icono-imagen");
        miniImagen.getChildren().add(icono);

        VBox datos = new VBox(2);
        Label titulo = new Label(producto.getTitulo());
        titulo.getStyleClass().add("nombre-sesion");
        Label detalle = new Label(nombreCategoria + "  ·  " + formatear(producto.getPrecio()));
        detalle.getStyleClass().add("hint");
        datos.getChildren().addAll(titulo, detalle);

        Region espaciador = new Region();
        HBox.setHgrow(espaciador, Priority.ALWAYS);

        Label estadoStock = producto.estaAgotado()
                ? new Label("Agotado")
                : new Label("Disponible: " + producto.getStock());
        estadoStock.getStyleClass().add(producto.estaAgotado() ? "texto-error" : "texto-apagado");

        Button btnCarrito = new Button("Al carrito");
        btnCarrito.getStyleClass().add("btn");
        btnCarrito.setDisable(producto.estaAgotado());
        btnCarrito.setOnAction(e -> alCarrito(producto));

        Button btnVer = new Button("Ver detalle");
        btnVer.getStyleClass().add("btn");
        btnVer.setOnAction(e -> alAbrirDetalle.accept(producto.getId()));

        Button btnQuitar = new Button();
        btnQuitar.getStyleClass().add("btn-quitar");
        btnQuitar.setGraphic(new Icono(Iconos.CERRAR, 16));
        btnQuitar.setTooltip(new Tooltip("Quitar de deseos"));
        btnQuitar.setOnAction(e -> quitar(producto));

        HBox fila = new HBox(12, miniImagen, datos, espaciador, estadoStock, btnCarrito, btnVer, btnQuitar);
        fila.getStyleClass().add("fila-lista");
        fila.setAlignment(Pos.CENTER_LEFT);
        return fila;
    }

    private void alCarrito(Producto producto) {
        try {
            app.getServicioCarrito().agregar(producto.getId(), 1);
            alCambiarCarrito.run();
            avisador.accept(Avisos.Tipo.EXITO, "\""
                    + producto.getTitulo() + "\" se agregó al carrito");
        } catch (ExcepcionNegocio e) {
            avisador.accept(Avisos.Tipo.ERROR, e.getMessage());
        }
    }

    private void quitar(Producto producto) {
        app.getServicioDeseos().quitar(idSesion(), producto.getId());
        refrescar();
    }

    private String idSesion() {
        return app.getSesion().getUsuario().getId();
    }

    private static String formatear(double valor) {
        return String.format(Locale.US, "$%,.2f", valor);
    }
}
