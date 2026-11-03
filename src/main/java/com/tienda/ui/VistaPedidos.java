package com.tienda.ui;

import com.tienda.modelo.EstadoPedido;
import com.tienda.modelo.ItemPedido;
import com.tienda.modelo.Pedido;
import com.tienda.servicio.ExcepcionNegocio;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.Locale;
import java.util.function.BiConsumer;

/**
 * Pedidos del cliente: historial de compras con su estado de despacho
 * (PENDIENTE → ENVIADO → ENTREGADO) y cancelación de los pendientes, que
 * restaura el stock de los ítems.
 */
public class VistaPedidos {

    private final GestorEscenas app;
    private final BiConsumer<Avisos.Tipo, String> avisador;

    private final VBox raiz = new VBox(14);
    private final VBox listaPedidos = new VBox(12);
    private final Label etContador = new Label();

    public VistaPedidos(GestorEscenas app, BiConsumer<Avisos.Tipo, String> avisador) {
        this.app = app;
        this.avisador = avisador;
        construir();
        refrescar();
    }

    public Node getRaiz() {
        return raiz;
    }

    private void construir() {
        raiz.setPadding(new Insets(18, 24, 20, 24));

        Label titulo = new Label("Mis pedidos");
        titulo.getStyleClass().add("titulo-panel");
        etContador.getStyleClass().add("texto-apagado");
        HBox cabecera = new HBox(10, titulo, etContador);
        cabecera.setAlignment(Pos.CENTER_LEFT);

        ScrollPane desplazamiento = new ScrollPane(listaPedidos);
        desplazamiento.setFitToWidth(true);
        desplazamiento.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        desplazamiento.getStyleClass().add("zona-scroll");
        VBox.setVgrow(desplazamiento, Priority.ALWAYS);

        raiz.getChildren().addAll(cabecera, desplazamiento);
    }

    private void refrescar() {
        List<Pedido> pedidos = app.getServicioPedidos().listarPorCliente(idSesion());
        etContador.setText(pedidos.size()
                + (pedidos.size() == 1 ? " pedido" : " pedidos"));

        listaPedidos.getChildren().clear();
        if (pedidos.isEmpty()) {
            listaPedidos.getChildren().add(estadoVacio());
            return;
        }
        for (Pedido pedido : pedidos) {
            listaPedidos.getChildren().add(tarjetaPedido(pedido));
        }
    }

    private Node estadoVacio() {
        VBox tarjeta = new VBox(10);
        tarjeta.getStyleClass().addAll("panel", "estado-vacio");
        tarjeta.setAlignment(Pos.CENTER);
        tarjeta.setPadding(new Insets(48));
        Icono icono = new Icono(Iconos.PAQUETE, 36);
        icono.getStyleClass().add("icono-modulo");
        Label titulo = new Label("Todavía no tienes pedidos");
        titulo.getStyleClass().add("titulo-modulo");
        Label pista = new Label("Cuando finalices una compra aparecerá aquí con su estado de despacho");
        pista.getStyleClass().add("texto-apagado");
        tarjeta.getChildren().addAll(icono, titulo, pista);
        return tarjeta;
    }

    private Node tarjetaPedido(Pedido pedido) {
        VBox tarjeta = new VBox(10);
        tarjeta.getStyleClass().add("panel");
        tarjeta.setPadding(new Insets(18));

        // --- Encabezado: ID, fecha, estado ---
        Label id = new Label(pedido.getId());
        id.getStyleClass().add("nombre-sesion");
        Label fecha = new Label(pedido.getFecha());
        fecha.getStyleClass().add("hint");
        Region espaciador = new Region();
        HBox.setHgrow(espaciador, Priority.ALWAYS);
        Label chipEstado = new Label(pedido.getEstado().toString());
        chipEstado.getStyleClass().addAll("chip", chipDeEstado(pedido.getEstado()));

        HBox encabezado = new HBox(10, id, fecha, espaciador, chipEstado);
        encabezado.setAlignment(Pos.CENTER_LEFT);

        // --- Ítems ---
        VBox items = new VBox(4);
        for (ItemPedido item : pedido.getItems()) {
            HBox fila = new HBox(8);
            fila.setAlignment(Pos.CENTER_LEFT);
            Label descripcion = new Label(item.getCantidad() + " × " + item.getTitulo());
            Region espaciadorItem = new Region();
            HBox.setHgrow(espaciadorItem, Priority.ALWAYS);
            Label valor = new Label(formatear(item.subtotal()));
            fila.getChildren().addAll(descripcion, espaciadorItem, valor);
            items.getChildren().add(fila);
        }

        // --- Totales ---
        Label lineaTotales = new Label("Subtotal " + formatear(pedido.getSubtotal())
                + "   ·   Impuesto (19%) " + formatear(pedido.getImpuesto())
                + "   ·   Total " + formatear(pedido.getTotal()));
        lineaTotales.getStyleClass().add("texto-apagado");

        tarjeta.getChildren().addAll(encabezado, items, lineaTotales);

        if (pedido.getEstado() == EstadoPedido.PENDIENTE) {
            Button btnCancelar = new Button("Cancelar pedido");
            btnCancelar.getStyleClass().add("btn");
            btnCancelar.setOnAction(e -> cancelar(pedido));
            tarjeta.getChildren().add(btnCancelar);
        }
        return tarjeta;
    }

    /** Color del chip según el estado del pedido. */
    private static String chipDeEstado(EstadoPedido estado) {
        return switch (estado) {
            case PENDIENTE -> "chip-alerta";
            case ENVIADO -> "chip-info";
            case ENTREGADO -> "chip-exito";
            case CANCELADO -> "chip-peligro";
        };
    }

    private void cancelar(Pedido pedido) {
        try {
            app.getServicioPedidos().cancelar(pedido.getId(), idSesion());
            refrescar();
            avisador.accept(Avisos.Tipo.INFO, "Pedido " + pedido.getId()
                    + " cancelado. El stock fue restaurado.");
        } catch (ExcepcionNegocio e) {
            avisador.accept(Avisos.Tipo.ERROR, e.getMessage());
        }
    }

    private String idSesion() {
        return app.getSesion().getUsuario().getId();
    }

    private static String formatear(double valor) {
        return String.format(Locale.US, "$%,.2f", valor);
    }
}
