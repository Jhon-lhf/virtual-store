package com.tienda.ui;

import com.tienda.modelo.Categoria;
import com.tienda.modelo.Producto;
import com.tienda.servicio.CriterioOrden;
import com.tienda.servicio.ServicioProductos;
import com.tienda.util.Pagina;
import com.tienda.util.Paginador;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

/**
 * Catálogo del cliente: barra de filtros (texto, categoría, precio, orden),
 * cuadrícula de tarjetas y paginación con tamaños 8/12/20.
 *
 * <p>Reglas heredadas del diseño: cambiar cualquier filtro vuelve a la
 * página 1, y el corte de página es puro ({@code Paginador}) con clamp:
 * nunca se queda al usuario en una página inexistente.</p>
 */
public class VistaCatalogo {

    private static final String SIN_CATEGORIA = "Todas las categorías";

    private final GestorEscenas app;
    private final Consumer<String> avisador;
    private final VBox raiz = new VBox(14);
    private final FlowPane cuadricula = new FlowPane(14, 14);
    private final TextField campoBusqueda = new TextField();
    private final TextField campoPrecioMin = new TextField();
    private final TextField campoPrecioMax = new TextField();
    private final ComboBox<String> comboCategoria = new ComboBox<>();
    private final ComboBox<String> comboOrden = new ComboBox<>();
    private final ComboBox<String> comboTamano = new ComboBox<>();
    private final Label etiquetaResumen = new Label();
    private final HBox barraPaginador = new HBox(6);
    private List<Categoria> categoriasDisponibles = List.of();
    private int paginaActual = 1;

    public VistaCatalogo(GestorEscenas app, Consumer<String> avisador) {
        this.app = app;
        this.avisador = avisador;
        construir();
        aplicarFiltros(true);
    }

    public Node getRaiz() {
        return raiz;
    }

    private void construir() {
        raiz.setPadding(new Insets(18, 24, 20, 24));

        // --- Barra de filtros ---
        HBox filtros = new HBox(10);
        filtros.getStyleClass().addAll("panel", "barra-filtros");
        filtros.setAlignment(Pos.CENTER_LEFT);

        campoBusqueda.getStyleClass().add("campo");
        campoBusqueda.setPromptText("Buscar por título o descripción…");
        HBox.setHgrow(campoBusqueda, Priority.ALWAYS);

        cargarCategorias();
        comboCategoria.getStyleClass().add("campo");
        comboCategoria.setPrefWidth(190);

        campoPrecioMin.getStyleClass().add("campo");
        campoPrecioMin.setPromptText("Precio mín");
        campoPrecioMin.setPrefWidth(100);
        campoPrecioMax.getStyleClass().add("campo");
        campoPrecioMax.setPromptText("Precio máx");
        campoPrecioMax.setPrefWidth(100);

        comboOrden.getStyleClass().add("campo");
        comboOrden.setPrefWidth(190);
        for (CriterioOrden orden : CriterioOrden.values()) {
            comboOrden.getItems().add(orden.getEtiqueta());
        }
        comboOrden.setValue(CriterioOrden.RELEVANCIA.getEtiqueta());

        comboTamano.getStyleClass().add("campo");
        comboTamano.getItems().addAll("8", "12", "20");
        comboTamano.setValue(String.valueOf(Paginador.TAMANO_DEFECTO));

        Button btnFiltrar = new Button("Filtrar");
        btnFiltrar.getStyleClass().addAll("btn", "btn-primario");
        Button btnLimpiar = new Button("Limpiar");
        btnLimpiar.getStyleClass().add("btn");

        filtros.getChildren().addAll(campoBusqueda, comboCategoria, campoPrecioMin,
                campoPrecioMax, comboOrden, btnFiltrar, btnLimpiar);

        // --- Resumen de resultados ---
        Label etiquetaPorPagina = new Label("Por página:");
        etiquetaPorPagina.getStyleClass().add("texto-apagado");
        Region espaciador = new Region();
        HBox.setHgrow(espaciador, Priority.ALWAYS);
        HBox resumen = new HBox(10, etiquetaResumen, espaciador, etiquetaPorPagina, comboTamano);
        resumen.setAlignment(Pos.CENTER_LEFT);
        etiquetaResumen.getStyleClass().add("texto-apagado");

        // --- Cuadrícula con desplazamiento ---
        ScrollPane desplazamiento = new ScrollPane(cuadricula);
        desplazamiento.setFitToWidth(true);
        desplazamiento.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        desplazamiento.getStyleClass().add("zona-scroll");
        VBox.setVgrow(desplazamiento, Priority.ALWAYS);
        cuadricula.setPrefWrapLength(1200);

        // --- Paginador ---
        barraPaginador.setAlignment(Pos.CENTER);

        raiz.getChildren().addAll(filtros, resumen, desplazamiento, barraPaginador);

        // --- Eventos: cualquier cambio de filtro reinicia a la página 1 ---
        campoBusqueda.setOnAction(e -> aplicarFiltros(true));
        btnFiltrar.setOnAction(e -> aplicarFiltros(true));
        btnLimpiar.setOnAction(e -> limpiarFiltros());
        comboCategoria.setOnAction(e -> aplicarFiltros(true));
        comboOrden.setOnAction(e -> aplicarFiltros(true));
        comboTamano.setOnAction(e -> aplicarFiltros(true));
    }

    /** Carga en el combo solo las categorías con productos publicados. */
    private void cargarCategorias() {
        categoriasDisponibles = app.getServicioCategorias().listarActivasConProductos();
        comboCategoria.getItems().setAll(SIN_CATEGORIA);
        for (Categoria categoria : categoriasDisponibles) {
            comboCategoria.getItems().add(categoria.getNombre());
        }
        comboCategoria.setValue(SIN_CATEGORIA);
    }

    private void aplicarFiltros(boolean reiniciarPagina) {
        List<Producto> resultados = app.getServicioProductos().buscar(leerCriterios());
        if (reiniciarPagina) {
            paginaActual = 1;
        }
        Pagina<Producto> pagina = Paginador.paginar(resultados, paginaActual, tamanoSeleccionado());
        paginaActual = pagina.numero();

        etiquetaResumen.setText(resultados.size() + (resultados.size() == 1 ? " producto" : " productos"));
        pintarTarjetas(pagina);
        pintarPaginador(pagina);
    }

    private ServicioProductos.CriteriosBusqueda leerCriterios() {
        String nombreCategoria = comboCategoria.getValue();
        String categoriaId = null;
        if (nombreCategoria != null && !nombreCategoria.equals(SIN_CATEGORIA)) {
            categoriaId = categoriasDisponibles.stream()
                    .filter(c -> c.getNombre().equals(nombreCategoria))
                    .map(Categoria::getId)
                    .findFirst()
                    .orElse(null);
        }
        CriterioOrden orden = CriterioOrden.values()
                [Math.max(0, comboOrden.getSelectionModel().getSelectedIndex())];
        return new ServicioProductos.CriteriosBusqueda(
                campoBusqueda.getText(),
                categoriaId,
                parsearPrecio(campoPrecioMin),
                parsearPrecio(campoPrecioMax),
                orden);
    }

    /** Acepta coma o punto decimal; un valor inválido se ignora (null). */
    private Double parsearPrecio(TextField campo) {
        String texto = campo.getText() == null ? "" : campo.getText().trim().replace(',', '.');
        if (texto.isEmpty()) {
            return null;
        }
        try {
            return Double.parseDouble(texto);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private int tamanoSeleccionado() {
        try {
            return Integer.parseInt(comboTamano.getValue());
        } catch (NumberFormatException e) {
            return Paginador.TAMANO_DEFECTO;
        }
    }

    private void limpiarFiltros() {
        campoBusqueda.clear();
        campoPrecioMin.clear();
        campoPrecioMax.clear();
        comboCategoria.setValue(SIN_CATEGORIA);
        comboOrden.setValue(CriterioOrden.RELEVANCIA.getEtiqueta());
        aplicarFiltros(true);
    }

    private void pintarTarjetas(Pagina<Producto> pagina) {
        cuadricula.getChildren().clear();
        if (pagina.elementos().isEmpty()) {
            cuadricula.getChildren().add(tarjetaEstadoVacio());
            return;
        }
        for (Producto producto : pagina.elementos()) {
            String nombreCategoria = app.getServicioProductos().nombreCategoria(producto.getCategoriaId());
            cuadricula.getChildren().add(new VistaTarjetaProducto(producto, nombreCategoria, () ->
                    avisador.accept("El detalle de producto llega en el próximo hito")));
        }
    }

    private Node tarjetaEstadoVacio() {
        VBox tarjeta = new VBox(8);
        tarjeta.getStyleClass().addAll("panel", "estado-vacio");
        tarjeta.setPrefWidth(1200);
        tarjeta.setAlignment(Pos.CENTER);
        tarjeta.setPadding(new Insets(48));
        Label icono = new Label("🔍");
        icono.getStyleClass().add("icono-modulo");
        Label titulo = new Label("Sin resultados");
        titulo.getStyleClass().add("titulo-modulo");
        Label pista = new Label("Prueba con otros términos de búsqueda o quita algunos filtros");
        pista.getStyleClass().add("texto-apagado");
        tarjeta.getChildren().addAll(icono, titulo, pista);
        return tarjeta;
    }

    private void pintarPaginador(Pagina<Producto> pagina) {
        barraPaginador.getChildren().clear();
        Label etiquetaPagina = new Label("Página " + pagina.numero() + " de " + pagina.totalPaginas());
        etiquetaPagina.getStyleClass().add("texto-apagado");

        if (pagina.totalPaginas() <= 1) {
            barraPaginador.getChildren().add(etiquetaPagina);
            return;
        }

        Button btnAtras = new Button("‹");
        btnAtras.getStyleClass().addAll("btn", "btn-paginador");
        btnAtras.setDisable(pagina.numero() <= 1);
        btnAtras.setOnAction(e -> {
            paginaActual = pagina.numero() - 1;
            aplicarFiltros(false);
        });

        Button btnAdelante = new Button("›");
        btnAdelante.getStyleClass().addAll("btn", "btn-paginador");
        btnAdelante.setDisable(pagina.numero() >= pagina.totalPaginas());
        btnAdelante.setOnAction(e -> {
            paginaActual = pagina.numero() + 1;
            aplicarFiltros(false);
        });

        barraPaginador.getChildren().add(btnAtras);
        for (int numero = 1; numero <= pagina.totalPaginas(); numero++) {
            Button btnPagina = new Button(String.valueOf(numero));
            btnPagina.getStyleClass().addAll("btn", "btn-paginador");
            if (numero == pagina.numero()) {
                btnPagina.getStyleClass().add("btn-paginador-activa");
            }
            int destino = numero;
            btnPagina.setOnAction(e -> {
                paginaActual = destino;
                aplicarFiltros(false);
            });
            barraPaginador.getChildren().add(btnPagina);
        }
        barraPaginador.getChildren().addAll(btnAdelante, etiquetaPagina);
    }
}
