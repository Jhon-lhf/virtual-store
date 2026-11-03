package com.tienda.ui;

import com.tienda.modelo.Producto;
import com.tienda.modelo.Resena;
import com.tienda.modelo.Rol;
import com.tienda.servicio.ExcepcionNegocio;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
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
 * Detalle de un producto: ficha con imagen, precio, stock y descripción;
 * acciones de carrito y deseos (solo para el rol CLIENTE); y la sección de
 * reseñas con el formulario para publicar o actualizar la propia.
 *
 * <p>La vista vive dentro de la pila de navegación del shell, así que los
 * botones atrás/adelante de la barra de ruta gobiernan el retorno.</p>
 */
public class VistaDetalleProducto {

    private static final String[] ETIQUETAS_CALIFICACION = {
            "1 ★", "2 ★★", "3 ★★★", "4 ★★★★", "5 ★★★★★"
    };

    private final GestorEscenas app;
    private final String productoId;
    private final BiConsumer<Avisos.Tipo, String> avisador;
    private final Runnable alCambiarCarrito;

    private final VBox raiz = new VBox(16);
    private final VBox listaResenas = new VBox(12);
    private final Label cantidadEtiqueta = new Label("1");
    private final Button btnMenos = new Button("−");
    private final Button btnMas = new Button("+");
    private final Button btnDeseo = new Button();
    private final ComboBox<String> comboCalificacion = new ComboBox<>();
    private final TextArea campoComentario = new TextArea();
    private final Button btnPublicar = new Button("Publicar reseña");
    private int cantidad = 1;

    public VistaDetalleProducto(GestorEscenas app, String productoId,
                                BiConsumer<Avisos.Tipo, String> avisador,
                                Runnable alCambiarCarrito) {
        this.app = app;
        this.productoId = productoId;
        this.avisador = avisador;
        this.alCambiarCarrito = alCambiarCarrito;
        construir();
    }

    public Node getRaiz() {
        return raiz;
    }

    private void construir() {
        raiz.setPadding(new Insets(18, 24, 20, 24));

        Producto producto = app.getServicioProductos().obtenerPorId(productoId).orElse(null);
        if (producto == null) {
            VBox panel = new VBox(8);
            panel.getStyleClass().addAll("panel", "estado-vacio");
            panel.setAlignment(Pos.CENTER);
            panel.setPadding(new Insets(48));
            Label titulo = new Label("Producto no disponible");
            titulo.getStyleClass().add("titulo-modulo");
            Label pista = new Label("Puede que el vendedor lo haya retirado del catálogo");
            pista.getStyleClass().add("texto-apagado");
            panel.getChildren().addAll(titulo, pista);
            raiz.getChildren().add(panel);
            return;
        }

        raiz.getChildren().add(panelPrincipal(producto));
        if (app.getSesion().getUsuario().getRol() == Rol.CLIENTE) {
            raiz.getChildren().add(panelAcciones(producto));
        }
        raiz.getChildren().add(panelResenas(producto));
    }

    // ---------------------------------------------------------------- ficha

    private Node panelPrincipal(Producto producto) {
        String nombreCategoria = app.getServicioProductos().nombreCategoria(producto.getCategoriaId());

        StackPane areaImagen = new StackPane();
        areaImagen.getStyleClass().add("detalle-imagen");
        areaImagen.setStyle("-fx-background-color: " + VistaTarjetaProducto.gradienteDe(nombreCategoria) + ";");
        Icono icono = new Icono(VistaTarjetaProducto.iconoDe(nombreCategoria), 72);
        icono.getStyleClass().add("icono-imagen");
        areaImagen.getChildren().add(icono);

        VBox info = new VBox(10);
        info.setAlignment(Pos.CENTER_LEFT);

        Label titulo = new Label(producto.getTitulo());
        titulo.getStyleClass().add("titulo-detalle");
        titulo.setWrapText(true);

        HBox filaRating = new HBox(8);
        filaRating.setAlignment(Pos.CENTER_LEFT);
        if (producto.totalResenas() == 0) {
            Label sinResenas = new Label("Sin reseñas todavía");
            sinResenas.getStyleClass().add("texto-apagado");
            filaRating.getChildren().add(sinResenas);
        } else {
            Label estrellas = new Label("★ " + String.format(Locale.US, "%.1f", producto.calificacionPromedio()));
            estrellas.getStyleClass().add("estrellas");
            Label total = new Label("· " + producto.totalResenas()
                    + (producto.totalResenas() == 1 ? " reseña" : " reseñas"));
            total.getStyleClass().add("texto-apagado");
            filaRating.getChildren().addAll(estrellas, total);
        }

        Label chip = new Label(nombreCategoria);
        chip.getStyleClass().add("chip");

        Label precio = new Label(formatear(producto.getPrecio()));
        precio.getStyleClass().add("precio-grande");

        Label stock = producto.estaAgotado()
                ? new Label("Agotado por el momento")
                : new Label("Disponible: " + producto.getStock()
                + (producto.getStock() == 1 ? " unidad" : " unidades"));
        stock.getStyleClass().add(producto.estaAgotado() ? "texto-error" : "texto-apagado");

        Label descripcion = new Label(producto.getDescripcion());
        descripcion.getStyleClass().add("texto-apagado");
        descripcion.setWrapText(true);

        info.getChildren().addAll(titulo, filaRating, chip, precio, stock, descripcion);

        HBox panel = new HBox(24, areaImagen, info);
        panel.getStyleClass().addAll("panel", "panel-detalle");
        panel.setPadding(new Insets(24));
        HBox.setHgrow(info, Priority.ALWAYS);
        return panel;
    }

    // ---------------------------------------------------------------- acciones

    private Node panelAcciones(Producto producto) {
        boolean deseado = app.getServicioDeseos().esDeseado(idSesion(), productoId);
        configurarBotonDeseo(deseado);
        btnDeseo.setOnAction(e -> alternarDeseo());

        btnMenos.getStyleClass().addAll("btn", "btn-cantidad");
        btnMenos.setOnAction(e -> {
            if (cantidad > 1) {
                cantidad--;
                refrescarCantidad(producto);
            }
        });

        cantidadEtiqueta.getStyleClass().add("cantidad-elegida");
        cantidadEtiqueta.setMinWidth(34);
        cantidadEtiqueta.setAlignment(Pos.CENTER);

        btnMas.getStyleClass().addAll("btn", "btn-cantidad");
        btnMas.setOnAction(e -> {
            if (cantidad < producto.getStock()) {
                cantidad++;
                refrescarCantidad(producto);
            }
        });

        Button btnAgregar = new Button("Agregar al carrito");
        btnAgregar.getStyleClass().addAll("btn", "btn-primario");
        btnAgregar.setDisable(producto.estaAgotado());
        btnAgregar.setOnAction(e -> agregarAlCarrito(producto));

        HBox acciones = new HBox(12);
        acciones.setAlignment(Pos.CENTER_LEFT);
        acciones.getChildren().addAll(btnMenos, cantidadEtiqueta, btnMas, btnAgregar, btnDeseo);
        refrescarCantidad(producto);
        return acciones;
    }

    /** Refresca la cantidad mostrada y el estado de los botones − / +. */
    private void refrescarCantidad(Producto producto) {
        cantidadEtiqueta.setText(String.valueOf(cantidad));
        btnMenos.setDisable(cantidad <= 1);
        btnMas.setDisable(cantidad >= producto.getStock());
    }

    private void agregarAlCarrito(Producto producto) {
        try {
            app.getServicioCarrito().agregar(productoId, cantidad);
            alCambiarCarrito.run();
            avisador.accept(Avisos.Tipo.EXITO, "Se agregaron " + cantidad + " × \""
                    + producto.getTitulo() + "\" al carrito");
        } catch (ExcepcionNegocio e) {
            avisador.accept(Avisos.Tipo.ERROR, e.getMessage());
        }
    }

    private void alternarDeseo() {
        boolean deseado = app.getServicioDeseos().esDeseado(idSesion(), productoId);
        try {
            if (deseado) {
                app.getServicioDeseos().quitar(idSesion(), productoId);
                avisador.accept(Avisos.Tipo.INFO, "Quitado de tus deseos");
            } else {
                app.getServicioDeseos().agregar(idSesion(), productoId);
                avisador.accept(Avisos.Tipo.EXITO, "Guardado en tus deseos");
            }
        } catch (ExcepcionNegocio e) {
            avisador.accept(Avisos.Tipo.ERROR, e.getMessage());
            return;
        }
        configurarBotonDeseo(!deseado);
    }

    private void configurarBotonDeseo(boolean deseado) {
        btnDeseo.getStyleClass().remove("btn-deseo-activo");
        if (deseado) {
            btnDeseo.getStyleClass().addAll("btn", "btn-deseo-activo");
            btnDeseo.setGraphic(new Icono(Iconos.DESEOS, 18));
            btnDeseo.setText("En tus deseos");
        } else {
            btnDeseo.getStyleClass().add("btn");
            btnDeseo.setGraphic(new Icono(Iconos.DESEOS_CONTORNO, 18));
            btnDeseo.setText("Agregar a deseos");
        }
    }

    // ---------------------------------------------------------------- reseñas

    private Node panelResenas(Producto producto) {
        VBox panel = new VBox(14);
        panel.getStyleClass().add("panel");
        panel.setPadding(new Insets(24));

        Label titulo = new Label("Reseñas (" + producto.totalResenas() + ")");
        titulo.getStyleClass().add("titulo-panel");

        refrescarListaResenas();

        panel.getChildren().addAll(titulo, listaResenas);
        if (app.getSesion().getUsuario().getRol() == Rol.CLIENTE) {
            panel.getChildren().add(formularioResena());
        }
        return panel;
    }

    private void refrescarListaResenas() {
        List<Resena> resenas = app.getServicioResenas().listarPorProducto(productoId);
        listaResenas.getChildren().clear();
        if (resenas.isEmpty()) {
            Label vacio = new Label("Sé el primero en contar cómo te fue con este producto.");
            vacio.getStyleClass().add("texto-apagado");
            listaResenas.getChildren().add(vacio);
            return;
        }
        for (Resena resena : resenas) {
            listaResenas.getChildren().add(filaResena(resena));
        }
    }

    private Node filaResena(Resena resena) {
        StackPane avatar = new StackPane(new Label(
                resena.getAutorNombre().substring(0, 1).toUpperCase()));
        avatar.getStyleClass().add("avatar");

        VBox contenido = new VBox(4);
        HBox filaEncabezado = new HBox(8);
        filaEncabezado.setAlignment(Pos.CENTER_LEFT);
        Label autor = new Label(resena.getAutorNombre());
        autor.getStyleClass().add("nombre-sesion");
        Label fecha = new Label(resena.getFecha());
        fecha.getStyleClass().add("hint");
        Region espaciador = new Region();
        HBox.setHgrow(espaciador, Priority.ALWAYS);
        Label estrellas = new Label(estrellas(resena.getCalificacion()));
        estrellas.getStyleClass().add("estrellas");
        filaEncabezado.getChildren().addAll(autor, fecha, espaciador, estrellas);

        Label comentario = new Label(resena.getComentario());
        comentario.setWrapText(true);

        contenido.getChildren().addAll(filaEncabezado, comentario);

        HBox fila = new HBox(12, avatar, contenido);
        fila.getStyleClass().add("fila-resena");
        fila.setAlignment(Pos.TOP_LEFT);
        HBox.setHgrow(contenido, Priority.ALWAYS);
        return fila;
    }

    private Node formularioResena() {
        comboCalificacion.getStyleClass().add("campo");
        comboCalificacion.getItems().addAll(ETIQUETAS_CALIFICACION);
        comboCalificacion.setPromptText("Calificación");

        campoComentario.getStyleClass().add("campo-texto");
        campoComentario.setPromptText("¿Qué te pareció el producto?");
        campoComentario.setPrefRowCount(3);

        btnPublicar.getStyleClass().addAll("btn", "btn-primario");
        btnPublicar.setOnAction(e -> publicarResena());

        // Si ya reseñó, el formulario llega precargado para actualizar.
        app.getServicioResenas().resenaDe(productoId, idSesion()).ifPresent(resena -> {
            comboCalificacion.setValue(ETIQUETAS_CALIFICACION[resena.getCalificacion() - 1]);
            campoComentario.setText(resena.getComentario());
            btnPublicar.setText("Actualizar mi reseña");
        });

        VBox formulario = new VBox(10);
        formulario.getStyleClass().add("panel-demo");
        formulario.setAlignment(Pos.CENTER_LEFT);
        formulario.getChildren().addAll(comboCalificacion, campoComentario, btnPublicar);
        return formulario;
    }

    private void publicarResena() {
        int calificacion = comboCalificacion.getSelectionModel().getSelectedIndex() + 1;
        try {
            app.getServicioResenas().dejarResena(productoId, idSesion(),
                    app.getSesion().getUsuario().getNombre(),
                    calificacion, campoComentario.getText());
            avisador.accept(Avisos.Tipo.EXITO, "¡Gracias por tu reseña!");
            refrescarListaResenas();
        } catch (ExcepcionNegocio e) {
            avisador.accept(Avisos.Tipo.ERROR, e.getMessage());
        }
    }

    // ---------------------------------------------------------------- utilidades

    private String idSesion() {
        return app.getSesion().getUsuario().getId();
    }

    private static String estrellas(int calificacion) {
        return "★".repeat(Math.max(0, calificacion)) + "☆".repeat(Math.max(0, 5 - calificacion));
    }

    private static String formatear(double valor) {
        return String.format(Locale.US, "$%,.2f", valor);
    }
}
