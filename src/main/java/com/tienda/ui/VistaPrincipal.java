package com.tienda.ui;

import com.tienda.modelo.Rol;
import com.tienda.modelo.Usuario;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
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
 * Shell principal con navegación lateral, patrón idiomático de las apps de
 * escritorio: la barra lateral concentra marca, módulos del rol, tema y
 * sesión, y libera todo el alto de la ventana para el contenido. Solo el
 * Catálogo está operativo en el hito 1; el resto de módulos muestra una
 * vista "próximamente" que documenta lo planeado.
 */
public class VistaPrincipal {

    /** Definición de un módulo del shell. */
    public record Pestana(String id, String titulo, boolean disponible) {
    }

    private static final Map<Rol, List<Pestana>> PESTANAS_POR_ROL = Map.of(
            Rol.CLIENTE, List.of(
                    new Pestana("catalogo", "Catálogo", true),
                    new Pestana("carrito", "Carrito", false),
                    new Pestana("deseos", "Deseos", false),
                    new Pestana("pedidos", "Mis pedidos", false)),
            Rol.VENDEDOR, List.of(
                    new Pestana("cola", "Cola FIFO", false),
                    new Pestana("stock", "Mi stock", false),
                    new Pestana("nuevo", "Nuevo producto", false)),
            Rol.ADMINISTRADOR, List.of(
                    new Pestana("usuarios", "Usuarios", false),
                    new Pestana("global", "Catálogo global", false),
                    new Pestana("categorias", "Categorías", false),
                    new Pestana("metricas", "Métricas", false)));

    /** Icono vectorial de cada módulo (ver {@link Iconos}). */
    private static final Map<String, String> ICONOS_POR_PESTANA = Map.ofEntries(
            Map.entry("catalogo", Iconos.TIENDA),
            Map.entry("carrito", Iconos.CARRITO),
            Map.entry("deseos", Iconos.DESEOS),
            Map.entry("pedidos", Iconos.PAQUETE),
            Map.entry("cola", Iconos.CAMION),
            Map.entry("stock", Iconos.ETIQUETA),
            Map.entry("nuevo", Iconos.AGREGAR),
            Map.entry("usuarios", Iconos.PERSONAS),
            Map.entry("global", Iconos.GLOBO),
            Map.entry("categorias", Iconos.CAPAS),
            Map.entry("metricas", Iconos.GRAFICO));

    private static final Map<String, DatosModulo> MODULOS_PRONTOS = Map.of(
            "carrito", new DatosModulo("Carrito",
                    "Revisa tu compra antes del checkout: totales con impuesto del 19% y deshacer/rehacer con Ctrl+Z / Ctrl+Y.",
                    List.of("Valida el stock al cambiar cantidades", "El historial de undo vive solo en la sesión")),
            "deseos", new DatosModulo("Deseos",
                    "Guarda productos para después sin comprometer el stock de nadie.",
                    List.of("La lista se persiste con tu cuenta", "Mueve productos al carrito cuando quieras")),
            "pedidos", new DatosModulo("Mis pedidos",
                    "Sigue tus compras de punta a punta.",
                    List.of("Estados: PENDIENTE → ENVIADO → ENTREGADO", "Factura disponible por pedido")),
            "cola", new DatosModulo("Cola de despacho (FIFO)",
                    "Atiende los pedidos en orden de llegada, sin injusticias ni \"piches\".",
                    List.of("Despachar siguiente (→ ENVIADO)", "Marcar seleccionado ENTREGADO", "La cola no se reordena a propósito")),
            "stock", new DatosModulo("Mi stock",
                    "Controla el inventario de tus productos.",
                    List.of("Solo ves lo que publicaste", "Actualizar stock afecta lo que puede comprar el cliente")),
            "nuevo", new DatosModulo("Nuevo producto",
                    "Publica productos en el catálogo.",
                    List.of("Elige una categoría aprobada del selector", "¿Ninguna encaja? Solicita una nueva al admin", "La imagen se copia a assets/images")),
            "usuarios", new DatosModulo("Usuarios",
                    "Moderación de cuentas del sistema.",
                    List.of("Bloquear/Desbloquear (un bloqueado no puede entrar)", "Cambiar rol CLIENTE ↔ VENDEDOR")),
            "global", new DatosModulo("Catálogo global",
                    "Visión y control total del inventario.",
                    List.of("Todos los productos de todos los vendedores", "Eliminar producto seleccionado")),
            "categorias", new DatosModulo("Categorías",
                    "Gobierno de las categorías del sistema.",
                    List.of("Aprueba o rechaza solicitudes de vendedores", "Crea categorías directamente", "Solo categorías aprobadas aparecen al publicar")),
            "metricas", new DatosModulo("Métricas",
                    "Tablero de salud del negocio.",
                    List.of("Ingresos (excluye cancelados)", "Ventas por vendedor", "Pedidos por estado")));

    private record DatosModulo(String titulo, String descripcion, List<String> puntos) {
    }

    private final GestorEscenas app;
    private final BorderPane raiz = new BorderPane();
    private final StackPane capaAvisos = new StackPane();
    private final StackPane contenido = new StackPane();
    private final Map<String, Node> vistasConstruidas = new HashMap<>();
    private final Map<String, Button> botonesPestana = new HashMap<>();
    private final Button btnTema = new Button();
    private List<Pestana> pestanasDelRol;
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
        pestanasDelRol = PESTANAS_POR_ROL.get(usuario.getRol());

        raiz.setLeft(barraLateral(usuario));
        raiz.setCenter(contenido);

        seleccionar(pestanasDelRol.get(0));
    }

    // ---------------------------------------------------------------- barra lateral

    private VBox barraLateral(Usuario usuario) {
        VBox lateral = new VBox(4);
        lateral.getStyleClass().add("barra-lateral");
        lateral.setPrefWidth(248);
        lateral.setMinWidth(248);
        lateral.setPadding(new Insets(18, 12, 16, 14));

        // --- Marca ---
        HBox marca = new HBox(10);
        marca.getStyleClass().add("marca-fila");
        marca.setAlignment(Pos.CENTER_LEFT);
        StackPane logo = new StackPane(new Label("T"));
        logo.getStyleClass().add("marca-logo");
        Label nombreMarca = new Label("Tienda Virtual");
        nombreMarca.getStyleClass().add("marca");
        marca.getChildren().addAll(logo, nombreMarca);

        Region separador = new Region();
        separador.getStyleClass().add("linea-separador");
        VBox.setMargin(separador, new Insets(14, 0, 10, 0));

        // --- Módulos del rol ---
        VBox navegacion = new VBox(4);
        for (Pestana pestana : pestanasDelRol) {
            Button boton = new Button(pestana.titulo());
            boton.getStyleClass().add("nav-boton");
            boton.setGraphic(new Icono(ICONOS_POR_PESTANA.get(pestana.id()), 18));
            boton.setContentDisplay(ContentDisplay.LEFT);
            boton.setGraphicTextGap(12);
            boton.setMaxWidth(Double.MAX_VALUE);
            boton.setAlignment(Pos.BASELINE_LEFT);
            if (!pestana.disponible()) {
                boton.getStyleClass().add("nav-boton-pronta");
                boton.setTooltip(new Tooltip("Disponible en el próximo hito"));
            }
            boton.setOnAction(e -> seleccionar(pestana));
            botonesPestana.put(pestana.id(), boton);
            navegacion.getChildren().add(boton);
        }

        // --- Zona inferior: sesión, tema y salir ---
        Region espaciador = new Region();
        VBox.setVgrow(espaciador, Priority.ALWAYS);

        HBox chipSesion = new HBox(10);
        chipSesion.setAlignment(Pos.CENTER_LEFT);
        chipSesion.setPadding(new Insets(10, 8, 10, 8));
        StackPane avatar = new StackPane(new Label(usuario.getNombre().substring(0, 1).toUpperCase()));
        avatar.getStyleClass().add("avatar");
        VBox datosSesion = new VBox(1);
        Label nombreSesion = new Label(usuario.getNombre());
        nombreSesion.getStyleClass().add("nombre-sesion");
        Label rolSesion = new Label(usuario.getRol().toString());
        rolSesion.getStyleClass().add("hint");
        datosSesion.getChildren().addAll(nombreSesion, rolSesion);
        chipSesion.getChildren().addAll(avatar, datosSesion);

        actualizarBotonTema();
        btnTema.getStyleClass().addAll("nav-boton", "nav-boton-secundaria");
        btnTema.setMaxWidth(Double.MAX_VALUE);
        btnTema.setAlignment(Pos.BASELINE_LEFT);
        btnTema.setContentDisplay(ContentDisplay.LEFT);
        btnTema.setGraphicTextGap(12);
        btnTema.setOnAction(e -> {
            app.alternarTema();
            actualizarBotonTema();
        });

        Button btnSalir = new Button("Cerrar sesión");
        btnSalir.getStyleClass().addAll("nav-boton", "nav-boton-salir");
        btnSalir.setGraphic(new Icono(Iconos.SALIR, 18));
        btnSalir.setContentDisplay(ContentDisplay.LEFT);
        btnSalir.setGraphicTextGap(12);
        btnSalir.setMaxWidth(Double.MAX_VALUE);
        btnSalir.setAlignment(Pos.BASELINE_LEFT);
        btnSalir.setOnAction(e -> app.cerrarSesion());

        lateral.getChildren().addAll(marca, separador, navegacion, espaciador, chipSesion, btnTema, btnSalir);
        return lateral;
    }

    private void actualizarBotonTema() {
        btnTema.setGraphic(new Icono(app.isTemaOscuro() ? Iconos.SOL : Iconos.LUNA, 18));
        btnTema.setText(app.isTemaOscuro() ? "Modo claro" : "Modo oscuro");
    }

    // ---------------------------------------------------------------- selección

    private void seleccionar(Pestana pestana) {
        pestanaActiva = pestana.id();
        botonesPestana.forEach((id, boton) ->
                boton.getStyleClass().remove("nav-boton-activa"));
        botonesPestana.get(pestana.id()).getStyleClass().add("nav-boton-activa");
        contenido.getChildren().setAll(obtenerVista(pestana));
    }

    /** Construye (o reutiliza) la vista de un módulo; el catálogo conserva sus filtros. */
    private Node obtenerVista(Pestana pestana) {
        return vistasConstruidas.computeIfAbsent(pestana.id(), id -> {
            if ("catalogo".equals(id)) {
                return new VistaCatalogo(app, mensaje ->
                        Avisos.mostrar(capaAvisos, Avisos.Tipo.INFO, mensaje)).getRaiz();
            }
            DatosModulo datos = MODULOS_PRONTOS.get(id);
            return new VistaProximamente(ICONOS_POR_PESTANA.get(id), datos.titulo(),
                    datos.descripcion(), datos.puntos()).getRaiz();
        });
    }
}
