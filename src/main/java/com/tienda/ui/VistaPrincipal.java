package com.tienda.ui;

import com.tienda.modelo.Rol;
import com.tienda.modelo.Usuario;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Shell principal: cabecera global (marca, sesión, tema, cerrar sesión) más
 * una barra de pestañas según el rol. Solo el Catálogo está operativo en el
 * hito 1; el resto de pestañas muestran una vista "próximamente" que
 * documenta el módulo planeado.
 */
public class VistaPrincipal {

    /** Definición de una pestaña del shell. */
    public record Pestana(String id, String titulo, String icono, boolean disponible) {
    }

    private static final Map<Rol, List<Pestana>> PESTANAS_POR_ROL = Map.of(
            Rol.CLIENTE, List.of(
                    new Pestana("catalogo", "Catálogo", "🛍️", true),
                    new Pestana("carrito", "Carrito", "🛒", false),
                    new Pestana("deseos", "Deseos", "❤️", false),
                    new Pestana("pedidos", "Mis pedidos", "📦", false)),
            Rol.VENDEDOR, List.of(
                    new Pestana("cola", "Cola FIFO", "🚚", false),
                    new Pestana("stock", "Mi stock", "🏷️", false),
                    new Pestana("nuevo", "Nuevo producto", "➕", false)),
            Rol.ADMINISTRADOR, List.of(
                    new Pestana("usuarios", "Usuarios", "👥", false),
                    new Pestana("global", "Catálogo global", "🌐", false),
                    new Pestana("categorias", "Categorías", "🗂️", false),
                    new Pestana("metricas", "Métricas", "📊", false)));

    private static final Map<String, DatosModulo> MODULOS_PRONTOS = Map.of(
            "carrito", new DatosModulo("🛒", "Carrito",
                    "Revisa tu compra antes del checkout: totales con impuesto del 19% y deshacer/rehacer con Ctrl+Z / Ctrl+Y.",
                    List.of("Valida el stock al cambiar cantidades", "El historial de undo vive solo en la sesión")),
            "deseos", new DatosModulo("❤️", "Deseos",
                    "Guarda productos para después sin comprometer el stock de nadie.",
                    List.of("La lista se persiste con tu cuenta", "Mueve productos al carrito cuando quieras")),
            "pedidos", new DatosModulo("📦", "Mis pedidos",
                    "Sigue tus compras de punta a punta.",
                    List.of("Estados: PENDIENTE → ENVIADO → ENTREGADO", "Factura disponible por pedido")),
            "cola", new DatosModulo("🚚", "Cola de despacho (FIFO)",
                    "Atiende los pedidos en orden de llegada, sin injusticias ni \"piches\".",
                    List.of("Despachar siguiente (→ ENVIADO)", "Marcar seleccionado ENTREGADO", "La cola no se reordena a propósito")),
            "stock", new DatosModulo("🏷️", "Mi stock",
                    "Controla el inventario de tus productos.",
                    List.of("Solo ves lo que publicaste", "Actualizar stock afecta lo que puede comprar el cliente")),
            "nuevo", new DatosModulo("➕", "Nuevo producto",
                    "Publica productos en el catálogo.",
                    List.of("Elige una categoría aprobada del selector", "¿Ninguna encaja? Solicita una nueva al admin", "La imagen se copia a assets/images")),
            "usuarios", new DatosModulo("👥", "Usuarios",
                    "Moderación de cuentas del sistema.",
                    List.of("Bloquear/Desbloquear (un bloqueado no puede entrar)", "Cambiar rol CLIENTE ↔ VENDEDOR")),
            "global", new DatosModulo("🌐", "Catálogo global",
                    "Visión y control total del inventario.",
                    List.of("Todos los productos de todos los vendedores", "Eliminar producto seleccionado")),
            "categorias", new DatosModulo("🗂️", "Categorías",
                    "Gobierno de las categorías del sistema.",
                    List.of("Aprueba o rechaza solicitudes de vendedores", "Crea categorías directamente", "Solo categorías aprobadas aparecen al publicar")),
            "metricas", new DatosModulo("📊", "Métricas",
                    "Tablero de salud del negocio.",
                    List.of("Ingresos (excluye cancelados)", "Ventas por vendedor", "Pedidos por estado")));

    private record DatosModulo(String icono, String titulo, String descripcion, List<String> puntos) {
    }

    private final GestorEscenas app;
    private final BorderPane raiz = new BorderPane();
    private final StackPane capaAvisos = new StackPane();
    private final StackPane contenido = new StackPane();
    private final HBox barraPestanas = new HBox();
    private final Map<String, Node> vistasConstruidas = new HashMap<>();
    private final Map<String, Button> botonesPestana = new HashMap<>();
    private final Button btnTema = new Button();
    private List<Pestana> pestañasDelRol;
    private String pestanaActiva;

    public VistaPrincipal(GestorEscenas app) {
        this.app = app;
        construir();
    }

    public StackPane getRaiz() {
        StackPane envoltorio = new StackPane(raiz, capaAvisos);
        capaAvisos.setMouseTransparent(true);
        return envoltorio;
    }

    private void construir() {
        Usuario usuario = app.getSesion().getUsuario();
        pestañasDelRol = PESTANAS_POR_ROL.get(usuario.getRol());

        raiz.setTop(new VBox(cabecera(usuario), barraPestanas));
        raiz.setCenter(contenido);

        seleccionar(pestañasDelRol.get(0));
    }

    // ---------------------------------------------------------------- cabecera

    private HBox cabecera(Usuario usuario) {
        HBox cabecera = new HBox(12);
        cabecera.getStyleClass().add("cabecera");
        cabecera.setAlignment(Pos.CENTER_LEFT);
        cabecera.setPadding(new Insets(0, 20, 0, 20));

        StackPane logo = new StackPane(new Label("T"));
        logo.getStyleClass().add("marca-logo");
        Label marca = new Label("Tienda Virtual");
        marca.getStyleClass().add("marca");

        Region espaciador = new Region();
        HBox.setHgrow(espaciador, Priority.ALWAYS);

        StackPane avatar = new StackPane(new Label(usuario.getNombre().substring(0, 1).toUpperCase()));
        avatar.getStyleClass().add("avatar");
        Label sesion = new Label(usuario.getNombre() + "  ·  " + usuario.getRol());
        sesion.getStyleClass().add("chip-sesion");
        HBox chipSesion = new HBox(8, avatar, sesion);
        chipSesion.setAlignment(Pos.CENTER);

        actualizarTextoTema();
        btnTema.getStyleClass().add("btn");
        btnTema.setOnAction(e -> {
            app.alternarTema();
            actualizarTextoTema();
        });

        Button btnSalir = new Button("Cerrar sesión");
        btnSalir.getStyleClass().add("btn");
        btnSalir.setOnAction(e -> app.cerrarSesion());

        cabecera.getChildren().addAll(logo, marca, espaciador, chipSesion, btnTema, btnSalir);
        return cabecera;
    }

    private void actualizarTextoTema() {
        btnTema.setText(app.isTemaOscuro() ? "☀️ Claro" : "🌙 Oscuro");
    }

    // ---------------------------------------------------------------- pestañas

    private HBox barraPestanas() {
        barraPestanas.getStyleClass().add("barra-pestanas");
        barraPestanas.setPadding(new Insets(0, 20, 0, 20));
        for (Pestana pestana : pestañasDelRol) {
            Button boton = new Button(pestana.icono() + "  " + pestana.titulo());
            boton.getStyleClass().add("pestana");
            if (!pestana.disponible()) {
                boton.getStyleClass().add("pestana-pronta");
                boton.setTooltip(new Tooltip("Disponible en el próximo hito"));
            }
            boton.setOnAction(e -> seleccionar(pestana));
            botonesPestana.put(pestana.id(), boton);
            barraPestanas.getChildren().add(boton);
        }
        return barraPestanas;
    }

    private void seleccionar(Pestana pestana) {
        pestanaActiva = pestana.id();
        botonesPestana.forEach((id, boton) ->
                boton.getStyleClass().remove("pestana-activa"));
        botonesPestana.get(pestana.id()).getStyleClass().add("pestana-activa");
        contenido.getChildren().setAll(obtenerVista(pestana));
    }

    /** Construye (o reutiliza) la vista de una pestaña; el catálogo conserva sus filtros. */
    private Node obtenerVista(Pestana pestana) {
        return vistasConstruidas.computeIfAbsent(pestana.id(), id -> {
            if ("catalogo".equals(id)) {
                return new VistaCatalogo(app, mensaje ->
                        Avisos.mostrar(capaAvisos, Avisos.Tipo.INFO, mensaje)).getRaiz();
            }
            DatosModulo datos = MODULOS_PRONTOS.get(id);
            return new VistaProximamente(datos.icono(), datos.titulo(),
                    datos.descripcion(), datos.puntos()).getRaiz();
        });
    }
}
