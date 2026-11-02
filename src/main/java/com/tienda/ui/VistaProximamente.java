package com.tienda.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.util.List;

/**
 * Marcador de posición para los módulos que llegan en próximos hitos.
 * Describe el módulo planeado para que la aplicación "cuente su historia"
 * desde el primer día.
 */
public class VistaProximamente {

    private final StackPane raiz = new StackPane();

    public VistaProximamente(String rutaIcono, String titulo, String descripcion, List<String> puntos) {
        VBox tarjeta = new VBox(12);
        tarjeta.getStyleClass().add("panel");
        tarjeta.setMaxWidth(560);
        tarjeta.setPadding(new Insets(30));
        tarjeta.setAlignment(Pos.CENTER_LEFT);

        Icono iconoGrande = new Icono(rutaIcono, 36);
        iconoGrande.getStyleClass().add("icono-modulo");
        Label tituloModulo = new Label(titulo);
        tituloModulo.getStyleClass().add("titulo-modulo");
        Label descripcionModulo = new Label(descripcion);
        descripcionModulo.getStyleClass().add("texto-apagado");
        descripcionModulo.setWrapText(true);

        Label chip = new Label("Próximo hito");
        chip.getStyleClass().addAll("chip", "chip-info");

        tarjeta.getChildren().addAll(iconoGrande, tituloModulo, descripcionModulo, chip);
        for (String punto : puntos) {
            Label linea = new Label("•  " + punto);
            linea.getStyleClass().add("texto-apagado");
            linea.setWrapText(true);
            tarjeta.getChildren().add(linea);
        }

        raiz.getChildren().add(tarjeta);
        raiz.setPadding(new Insets(24));
    }

    public Node getRaiz() {
        return raiz;
    }
}
