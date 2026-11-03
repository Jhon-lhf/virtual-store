package com.tienda.ui;

import com.tienda.modelo.Pedido;
import com.tienda.servicio.ExcepcionNegocio;
import com.tienda.servicio.ServicioCarrito;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.Locale;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Carrito de la sesión: filas de ítems con control de cantidad, totales con
 * el impuesto del 19% y finalización de la compra.
 *
 * <p>El historial de deshacer/rehacer vive en {@code ServicioCarrito} (dos
 * pilas LIFO): los botones de la barra y los atajos Ctrl+Z / Ctrl+Y lo
 * gobiernan. Al finalizar la compra el historial se reinicia: un pedido ya
 * no se puede "deshacer".</p>
 */
public class VistaCarrito {

    private final GestorEscenas app;
    private final BiConsumer<Avisos.Tipo, String> avisador;
    private final Runnable alCambiarCarrito;
    private final Runnable alIrAlCatalogo;

    private final HBox raiz = new HBox(16);
    private final VBox listaItems = new VBox(10);
    private final Button btnDeshacer = new Button("Deshacer");
    private final Button btnRehacer = new Button("Rehacer");
    private final Button btnVaciar = new Button("Vaciar carrito");
    private final Button btnPagar = new Button("Finalizar compra");
    private final Label etSubtotal = new Label();
    private final Label etImpuesto = new Label();
    private final Label etTotal = new Label();
    private final Label etResumen = new Label();

    public VistaCarrito(GestorEscenas app, BiConsumer<Avisos.Tipo, String> avisador,
                        Runnable alCambiarCarrito, Runnable alIrAlCatalogo) {
        this.app = app;
        this.avisador = avisador;
        this.alCambiarCarrito = alCambiarCarrito;
        this.alIrAlCatalogo = alIrAlCatalogo;
        construir();
        refrescar();
    }

    public Node getRaiz() {
        return raiz;
    }

    private void construir() {
        raiz.setPadding(new Insets(18, 24, 20, 24));

        // --- Barra de acciones del historial ---
        btnDeshacer.getStyleClass().add("btn");
        btnDeshacer.setOnAction(e -> deshacer());
        btnRehacer.getStyleClass().add("btn");
        btnRehacer.setOnAction(e -> rehacer());
        Region espaciadorBarra = new Region();
        HBox.setHgrow(espaciadorBarra, Priority.ALWAYS);
        btnVaciar.getStyleClass().add("btn");
        btnVaciar.setOnAction(e -> vaciar());
        HBox barra = new HBox(8, btnDeshacer, btnRehacer, espaciadorBarra, btnVaciar);
        barra.setAlignment(Pos.CENTER_LEFT);

        // --- Lista de ítems con desplazamiento ---
        ScrollPane desplazamiento = new ScrollPane(listaItems);
        desplazamiento.setFitToWidth(true);
        desplazamiento.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        desplazamiento.getStyleClass().add("zona-scroll");
        VBox.setVgrow(desplazamiento, Priority.ALWAYS);

        VBox ladoIzquierdo = new VBox(12, barra, desplazamiento);
        HBox.setHgrow(ladoIzquierdo, Priority.ALWAYS);

        // --- Atajos de teclado: Ctrl+Z deshace, Ctrl+Y rehace ---
        raiz.addEventFilter(KeyEvent.KEY_PRESSED, e -> {
            if (!e.isControlDown() || e.isShiftDown()) {
                return;
            }
            if (e.getCode() == KeyCode.Z) {
                deshacer();
                e.consume();
            } else if (e.getCode() == KeyCode.Y) {
                rehacer();
                e.consume();
            }
        });

        raiz.getChildren().addAll(ladoIzquierdo, panelResumen());
    }

    // ---------------------------------------------------------------- resumen

    private Node panelResumen() {
        VBox panel = new VBox(10);
        panel.getStyleClass().addAll("panel", "panel-resumen");
        panel.setPadding(new Insets(20));
        panel.setMaxHeight(260);

        Label titulo = new Label("Resumen");
        titulo.getStyleClass().add("titulo-panel");
        etResumen.getStyleClass().add("texto-apagado");

        Region separador = new Region();
        separador.getStyleClass().add("linea-separador");
        VBox.setMargin(separador, new Insets(4, 0, 4, 0));

        HBox filaSubtotal = new HBox(8, new Label("Subtotal"), espaciadorFijo(), etSubtotal);
        HBox filaImpuesto = new HBox(8, new Label("Impuesto (19%)"), espaciadorFijo(), etImpuesto);
        HBox filaTotal = new HBox(8, new Label("Total"), espaciadorFijo(), etTotal);
        filaTotal.getStyleClass().add("fila-total");
        etTotal.getStyleClass().add("total-grande");

        btnPagar.getStyleClass().addAll("btn", "btn-primario");
        btnPagar.setMaxWidth(Double.MAX_VALUE);
        btnPagar.setOnAction(e -> finalizarCompra());

        panel.getChildren().addAll(titulo, etResumen, separador,
                filaSubtotal, filaImpuesto, separador2(), filaTotal, btnPagar);
        return panel;
    }

    private Region espaciadorFijo() {
        Region espaciador = new Region();
        HBox.setHgrow(espaciador, Priority.ALWAYS);
        return espaciador;
    }

    private Region separador2() {
        Region separador = new Region();
        separador.getStyleClass().add("linea-separador");
        VBox.setMargin(separador, new Insets(2, 0, 2, 0));
        return separador;
    }

    // ---------------------------------------------------------------- filas

    private void refrescar() {
        ServicioCarrito carrito = app.getServicioCarrito();
        java.util.List<ServicioCarrito.ItemDetallado> items = carrito.itemsDetallados();

        listaItems.getChildren().clear();
        if (items.isEmpty()) {
            listaItems.getChildren().add(estadoVacio());
        } else {
            for (ServicioCarrito.ItemDetallado item : items) {
                listaItems.getChildren().add(filaItem(item));
            }
        }

        ServicioCarrito.Totales totales = carrito.calcularTotales();
        etSubtotal.setText(formatear(totales.subtotal()));
        etImpuesto.setText(formatear(totales.impuesto()));
        etTotal.setText(formatear(totales.total()));
        int unidades = carrito.cantidadItems();
        etResumen.setText(unidades == 0 ? "Carrito vacío"
                : unidades + (unidades == 1 ? " unidad" : " unidades"));

        btnDeshacer.setDisable(!carrito.puedeDeshacer());
        btnRehacer.setDisable(!carrito.puedeRehacer());
        btnVaciar.setDisable(items.isEmpty());
        btnPagar.setDisable(items.isEmpty());
    }

    private Node estadoVacio() {
        VBox tarjeta = new VBox(10);
        tarjeta.getStyleClass().addAll("panel", "estado-vacio");
        tarjeta.setAlignment(Pos.CENTER);
        tarjeta.setPadding(new Insets(48));
        Icono icono = new Icono(Iconos.CARRITO, 36);
        icono.getStyleClass().add("icono-modulo");
        Label titulo = new Label("Tu carrito está vacío");
        titulo.getStyleClass().add("titulo-modulo");
        Label pista = new Label("Explora el catálogo y agrega lo que te guste");
        pista.getStyleClass().add("texto-apagado");
        Button btnCatalogo = new Button("Ir al catálogo");
        btnCatalogo.getStyleClass().addAll("btn", "btn-primario");
        btnCatalogo.setOnAction(e -> alIrAlCatalogo.run());
        tarjeta.getChildren().addAll(icono, titulo, pista, btnCatalogo);
        return tarjeta;
    }

    private Node filaItem(ServicioCarrito.ItemDetallado item) {
        String nombreCategoria = app.getServicioProductos().nombreCategoria(item.producto().getCategoriaId());

        StackPane miniImagen = new StackPane();
        miniImagen.getStyleClass().add("mini-imagen");
        miniImagen.setStyle("-fx-background-color: " + VistaTarjetaProducto.gradienteDe(nombreCategoria) + ";");
        Icono icono = new Icono(VistaTarjetaProducto.iconoDe(nombreCategoria), 24);
        icono.getStyleClass().add("icono-imagen");
        miniImagen.getChildren().add(icono);

        VBox datos = new VBox(2);
        Label titulo = new Label(item.producto().getTitulo());
        titulo.getStyleClass().add("nombre-sesion");
        Label precioUnitario = new Label(formatear(item.producto().getPrecio()) + " c/u");
        precioUnitario.getStyleClass().add("hint");
        datos.getChildren().addAll(titulo, precioUnitario);

        Button btnMenos = new Button("−");
        btnMenos.getStyleClass().addAll("btn", "btn-cantidad");
        btnMenos.setOnAction(e -> cambiarCantidad(item, item.cantidad() - 1));

        Label cantidad = new Label(String.valueOf(item.cantidad()));
        cantidad.getStyleClass().add("cantidad-elegida");
        cantidad.setMinWidth(30);
        cantidad.setAlignment(Pos.CENTER);

        Button btnMas = new Button("+");
        btnMas.getStyleClass().addAll("btn", "btn-cantidad");
        btnMas.setOnAction(e -> cambiarCantidad(item, item.cantidad() + 1));

        Region espaciador = new Region();
        HBox.setHgrow(espaciador, Priority.ALWAYS);

        Label subtotal = new Label(formatear(item.producto().getPrecio() * item.cantidad()));
        subtotal.getStyleClass().add("precio");

        Button btnQuitar = new Button();
        btnQuitar.getStyleClass().add("btn-quitar");
        btnQuitar.setGraphic(new Icono(Iconos.CERRAR, 16));
        btnQuitar.setTooltip(new javafx.scene.control.Tooltip("Quitar del carrito"));
        btnQuitar.setOnAction(e -> quitar(item));

        HBox fila = new HBox(12, miniImagen, datos, espaciador,
                btnMenos, cantidad, btnMas, subtotal, btnQuitar);
        fila.getStyleClass().add("fila-lista");
        fila.setAlignment(Pos.CENTER_LEFT);
        return fila;
    }

    // ---------------------------------------------------------------- acciones

    private void cambiarCantidad(ServicioCarrito.ItemDetallado item, int nuevaCantidad) {
        try {
            if (nuevaCantidad <= 0) {
                app.getServicioCarrito().quitar(item.producto().getId());
            } else {
                app.getServicioCarrito().cambiarCantidad(item.producto().getId(), nuevaCantidad);
            }
            refrescar();
            alCambiarCarrito.run();
        } catch (ExcepcionNegocio e) {
            avisador.accept(Avisos.Tipo.ALERTA, e.getMessage());
        }
    }

    private void quitar(ServicioCarrito.ItemDetallado item) {
        try {
            app.getServicioCarrito().quitar(item.producto().getId());
            refrescar();
            alCambiarCarrito.run();
        } catch (ExcepcionNegocio e) {
            avisador.accept(Avisos.Tipo.ERROR, e.getMessage());
        }
    }

    private void vaciar() {
        app.getServicioCarrito().vaciar();
        refrescar();
        alCambiarCarrito.run();
    }

    private void deshacer() {
        if (app.getServicioCarrito().deshacer()) {
            refrescar();
            alCambiarCarrito.run();
        }
    }

    private void rehacer() {
        if (app.getServicioCarrito().rehacer()) {
            refrescar();
            alCambiarCarrito.run();
        }
    }

    private void finalizarCompra() {
        try {
            Pedido pedido = app.getServicioPedidos().crearPedido(
                    new ArrayList<>(app.getSesion().getCarrito()),
                    app.getSesion().getUsuario().getId(),
                    app.getSesion().getUsuario().getNombre());
            // La compra ya es un pedido: el carrito y su historial se reinician.
            app.getServicioCarrito().reiniciar();
            refrescar();
            alCambiarCarrito.run();
            avisador.accept(Avisos.Tipo.EXITO, "Pedido " + pedido.getId()
                    + " creado. Podrás seguirlo en \"Mis pedidos\".");
        } catch (ExcepcionNegocio e) {
            avisador.accept(Avisos.Tipo.ERROR, e.getMessage());
        }
    }

    private static String formatear(double valor) {
        return String.format(Locale.US, "$%,.2f", valor);
    }
}
