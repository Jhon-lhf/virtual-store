package com.tienda.ui;

import com.tienda.modelo.Producto;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.util.Locale;

/**
 * Tarjeta de producto para la cuadrícula del catálogo: área de imagen
 * (gradiente + ícono por categoría), título, rating, chip de categoría,
 * precio y aviso de stock (solo cuando está bajo o agotado).
 */
public class VistaTarjetaProducto extends VBox {

    /** Gradientes de fondo para el área de imagen, rotados por categoría. */
    private static final String[] GRADIENTES = {
            "linear-gradient(to bottom right, #DBEAFE, #BFDBFE)",
            "linear-gradient(to bottom right, #FCE7F3, #FBCFE8)",
            "linear-gradient(to bottom right, #FEF3C7, #FDE68A)",
            "linear-gradient(to bottom right, #D1FAE5, #A7F3D0)",
            "linear-gradient(to bottom right, #E0E7FF, #C7D2FE)"
    };

    private final Producto producto;

    public VistaTarjetaProducto(Producto producto, String nombreCategoria, Runnable alHacerClic) {
        this.producto = producto;
        getStyleClass().add("tarjeta-producto");
        setPrefWidth(224);
        setOnMouseClicked(e -> alHacerClic.run());

        // --- Área de imagen (gradiente + ícono de la categoría) ---
        Label icono = new Label(iconoDe(nombreCategoria));
        icono.getStyleClass().add("icono-imagen");
        StackPane areaImagen = new StackPane(icono);
        areaImagen.getStyleClass().add("area-imagen");
        areaImagen.setPrefSize(224, 120);
        areaImagen.setMaxSize(Double.MAX_VALUE, 120);
        areaImagen.setStyle("-fx-background-color: " + gradienteDe(nombreCategoria) + ";");

        // --- Contenido ---
        VBox contenido = new VBox(6);
        contenido.setPadding(new Insets(12));

        Label titulo = new Label(producto.getTitulo());
        titulo.getStyleClass().add("titulo-producto");
        titulo.setWrapText(true);

        Label rating = new Label(textoRating());
        rating.getStyleClass().add(producto.totalResenas() == 0 ? "texto-apagado" : "rating");

        Label chip = new Label(nombreCategoria);
        chip.getStyleClass().add("chip");

        Label precio = new Label(formatearPrecio(producto.getPrecio()));
        precio.getStyleClass().add("precio");
        Region espaciador = new Region();
        HBox.setHgrow(espaciador, Priority.ALWAYS);
        HBox filaPrecio = new HBox(8, precio, espaciador);
        filaPrecio.setAlignment(Pos.CENTER_LEFT);
        Label avisoStock = avisoStock();
        if (avisoStock != null) {
            filaPrecio.getChildren().add(avisoStock);
        }

        contenido.getChildren().addAll(titulo, rating, chip, filaPrecio);
        getChildren().addAll(areaImagen, contenido);
    }

    private Label avisoStock() {
        if (producto.estaAgotado()) {
            Label aviso = new Label("AGOTADO");
            aviso.getStyleClass().addAll("chip", "chip-peligro");
            return aviso;
        }
        if (producto.tieneStockBajo()) {
            Label aviso = new Label("¡Bajo!");
            aviso.getStyleClass().addAll("chip", "chip-alerta");
            return aviso;
        }
        return null;
    }

    private String textoRating() {
        if (producto.totalResenas() == 0) {
            return "Sin reseñas";
        }
        return "★ " + String.format(Locale.US, "%.1f", producto.calificacionPromedio())
                + "  (" + producto.totalResenas() + ")";
    }

    private String formatearPrecio(double precio) {
        return String.format(Locale.US, "$%,.2f", precio);
    }

    private String iconoDe(String nombreCategoria) {
        return switch (nombreCategoria == null ? "" : nombreCategoria) {
            case "Tecnologia" -> "💻";
            case "Ropa" -> "👕";
            case "Hogar" -> "🛋️";
            case "Libros" -> "📚";
            case "Juguetes" -> "🧸";
            default -> "🛍️";
        };
    }

    private String gradienteDe(String nombreCategoria) {
        int indice = nombreCategoria == null
                ? 0
                : Math.abs(nombreCategoria.hashCode()) % GRADIENTES.length;
        return GRADIENTES[indice];
    }
}
