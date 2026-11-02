package com.tienda.ui;

import javafx.animation.FadeTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

import java.util.Locale;

/**
 * Avisos flotantes (toasts) para la interfaz. Toda acción que necesite
 * informar algo lo hace por aquí: nunca se falla en silencio.
 *
 * <p>La capa es un {@link StackPane} transparente sobre la vista; los avisos
 * aparecen abajo al centro y se desvanecen solos.</p>
 */
public final class Avisos {

    /** Severidad del aviso (define su color en el CSS). */
    public enum Tipo {
        INFO, EXITO, ALERTA, ERROR
    }

    private Avisos() {
    }

    /** Muestra un aviso en la capa dada; reemplaza al que estuviera visible. */
    public static void mostrar(StackPane capa, Tipo tipo, String mensaje) {
        Label etiqueta = new Label(mensaje);
        etiqueta.setWrapText(true);
        etiqueta.getStyleClass().addAll("toast", "toast-" + tipo.name().toLowerCase(Locale.ROOT));
        etiqueta.setOpacity(0);

        capa.getChildren().setAll(etiqueta);
        StackPane.setAlignment(etiqueta, Pos.BOTTOM_CENTER);
        StackPane.setMargin(etiqueta, new Insets(0, 0, 28, 0));

        FadeTransition aparecer = new FadeTransition(Duration.millis(150), etiqueta);
        aparecer.setToValue(1);
        FadeTransition desaparecer = new FadeTransition(Duration.millis(300), etiqueta);
        desaparecer.setDelay(Duration.millis(2400));
        desaparecer.setToValue(0);
        desaparecer.setOnFinished(e -> capa.getChildren().remove(etiqueta));
        aparecer.setOnFinished(e -> desaparecer.play());
        aparecer.play();
    }
}
