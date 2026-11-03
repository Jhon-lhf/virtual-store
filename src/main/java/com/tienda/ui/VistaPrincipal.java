package com.tienda.ui;

import com.tienda.modelo.Rol;
import com.tienda.modelo.Usuario;
import com.tienda.estructuras.PilaNavegacion;
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
 * sesión, y el contenido navega con una pila de rutas ({@link PilaNavegacion})
 * que da los botones atrás/adelante del detalle de producto.
 *
 * <p>Los módulos del cliente (catálogo, carrito, deseos, pedidos) están
 * operativos; los del vendedor y el admin muestran una vista "próximamente"
 * que documenta lo planeado.</p>
 */
public class VistaPrincipal {

    /** Definición de un módulo del shell. */
    public record Pestana(String id, String titulo, boolean disponible) {
    }

    private static final Map<Rol, List<Pestana>> PESTANAS_POR_ROL = Map.of(
            Rol.CLIENTE, List.of(
                    new Pestana("catalogo", "Catálogo", true),
                    new Pestana("carrito", "Carrito", true),
                    new Pestana("deseos", "Deseos", true),
                    new Pestana("pedidos", "Mis pedidos", true)),
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

    /** Prefijos de las rutas guardadas en la pila de navegación. */
    private static final String RUTA_MODULO = "M:";
    private static final String RUTA_DETALLE = "D:";

    private final GestorEscenas app;
    private final BorderPane raiz = new BorderPane();
    private final StackPane capaAvisos = new StackPane();
    private final StackPane contenido = new StackPane();
    private final PilaNavegacion pilaRutas = new PilaNavegacion();
    private final Map<String, Node> vistasConstruidas = new HashMap<>();
    private final Map<String, Button> botonesPestana = new HashMap<>();
    private final Button btnTema = new Button();
    private final Button btnAtras = new Button();
    private final Button btnAdelante = new Button();
    private final Label etiquetaRuta = new Label();
    private List<Pestana> pestanasDelRol;

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
        raiz.setCenter(new VBox(barraRuta(), contenido));
        VBox.setVgrow(contenido, Priority.ALWAYS);

        // Ruta inicial: el primer módulo del rol (sin apilar nada previo).
        String rutaInicial = RUTA_MODULO + pestanasDelRol.get(0).id();
        pilaRutas.visitar(rutaInicial);
        renderizar(rutaInicial);
        actualizarBotonesRuta();
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
            } else {
                boton.setOnAction(e -> navegarAModulo(pestana.id()));
            }
            botonesPestana.put(pestana.id(), boton);
            navegacion.getChildren().add(boton);
        }
        actualizarBadgeCarrito();

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

    /** Actualiza el contador que muestra el botón Carrito (badge del carrito). */
    public void actualizarBadgeCarrito() {
        Button btnCarrito = botonesPestana.get("carrito");
        if (btnCarrito == null) {
            return;
        }
        int unidades = app.getServicioCarrito().cantidadItems();
        btnCarrito.setText(unidades > 0 ? "Carrito (" + unidades + ")" : "Carrito");
    }

    // ---------------------------------------------------------------- navegación

    /** Barra fina sobre el contenido: atrás / adelante / título de la vista actual. */
    private Node barraRuta() {
        HBox barra = new HBox(8);
        barra.getStyleClass().add("barra-ruta");
        barra.setAlignment(Pos.CENTER_LEFT);

        btnAtras.getStyleClass().addAll("btn", "btn-icono");
        btnAtras.setGraphic(new Icono(Iconos.FLECHA_IZQUIERDA, 16));
        btnAtras.setTooltip(new Tooltip("Atrás"));
        btnAtras.setOnAction(e -> {
            pilaRutas.irAtras().ifPresent(this::renderizar);
            actualizarBotonesRuta();
        });

        btnAdelante.getStyleClass().addAll("btn", "btn-icono");
        btnAdelante.setGraphic(new Icono(Iconos.FLECHA_DERECHA, 16));
        btnAdelante.setTooltip(new Tooltip("Adelante"));
        btnAdelante.setOnAction(e -> {
            pilaRutas.irAdelante().ifPresent(this::renderizar);
            actualizarBotonesRuta();
        });

        etiquetaRuta.getStyleClass().add("texto-apagado");
        Region espaciador = new Region();
        HBox.setHgrow(espaciador, Priority.ALWAYS);

        barra.getChildren().addAll(btnAtras, btnAdelante, etiquetaRuta, espaciador);
        return barra;
    }

    private void actualizarBotonesRuta() {
        btnAtras.setDisable(!pilaRutas.puedeAtras());
        btnAdelante.setDisable(!pilaRutas.puedeAdelante());
    }

    /** Navega a un módulo del shell (botón de la barra lateral). */
    public void navegarAModulo(String idModulo) {
        navegarA(RUTA_MODULO + idModulo);
    }

    /** Navega al detalle de un producto (clic en una tarjeta del catálogo). */
    public void abrirDetalle(String productoId) {
        navegarA(RUTA_DETALLE + productoId);
    }

    private void navegarA(String ruta) {
        if (ruta.equals(pilaRutas.actual())) {
            return;
        }
        pilaRutas.visitar(ruta);
        renderizar(ruta);
        actualizarBotonesRuta();
    }

    /** Pinta la ruta dada y sincroniza el resaltado de la barra lateral. */
    private void renderizar(String ruta) {
        if (ruta.startsWith(RUTA_DETALLE)) {
            String productoId = ruta.substring(RUTA_DETALLE.length());
            etiquetaRuta.setText("");
            marcarModuloActivo(null);
            contenido.getChildren().setAll(new VistaDetalleProducto(app, productoId,
                    (tipo, mensaje) -> Avisos.mostrar(capaAvisos, tipo, mensaje),
                    this::actualizarBadgeCarrito).getRaiz());
            return;
        }

        String idModulo = ruta.substring(RUTA_MODULO.length());
        pestanasDelRol.stream()
                .filter(p -> p.id().equals(idModulo))
                .findFirst()
                .ifPresentOrElse(
                        pestana -> etiquetaRuta.setText(pestana.titulo()),
                        () -> etiquetaRuta.setText(""));
        marcarModuloActivo(idModulo);
        contenido.getChildren().setAll(obtenerVista(idModulo));
    }

    private void marcarModuloActivo(String idModulo) {
        botonesPestana.forEach((id, boton) ->
                boton.getStyleClass().remove("nav-boton-activa"));
        if (idModulo != null && botonesPestana.containsKey(idModulo)) {
            botonesPestana.get(idModulo).getStyleClass().add("nav-boton-activa");
        }
    }

    /**
     * Construye la vista de un módulo. El catálogo se cachea (conserva sus
     * filtros); carrito, deseos y pedidos se reconstruyen en cada visita para
     * mostrar siempre datos frescos.
     */
    private Node obtenerVista(String idModulo) {
        if ("catalogo".equals(idModulo)) {
            return vistasConstruidas.computeIfAbsent(idModulo, id -> {
                VistaCatalogo vista = new VistaCatalogo(app,
                        mensaje -> Avisos.mostrar(capaAvisos, Avisos.Tipo.INFO, mensaje),
                        this::abrirDetalle);
                return vista.getRaiz();
            });
        }
        if ("carrito".equals(idModulo)) {
            return new VistaCarrito(app,
                    (tipo, mensaje) -> Avisos.mostrar(capaAvisos, tipo, mensaje),
                    this::actualizarBadgeCarrito,
                    () -> navegarAModulo("catalogo")).getRaiz();
        }
        if ("deseos".equals(idModulo)) {
            return new VistaDeseos(app,
                    (tipo, mensaje) -> Avisos.mostrar(capaAvisos, tipo, mensaje),
                    this::actualizarBadgeCarrito,
                    this::abrirDetalle).getRaiz();
        }
        if ("pedidos".equals(idModulo)) {
            return new VistaPedidos(app,
                    (tipo, mensaje) -> Avisos.mostrar(capaAvisos, tipo, mensaje)).getRaiz();
        }
        DatosModulo datos = MODULOS_PRONTOS.get(idModulo);
        if (datos == null) {
            return new Label("Módulo no disponible");
        }
        return new VistaProximamente(ICONOS_POR_PESTANA.get(idModulo), datos.titulo(),
                datos.descripcion(), datos.puntos()).getRaiz();
    }
}
