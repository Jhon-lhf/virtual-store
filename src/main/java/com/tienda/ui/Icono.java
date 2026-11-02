package com.tienda.ui;

import javafx.scene.layout.Region;
/**
 * Región que pinta un icono vectorial mediante CSS ({@code -fx-shape}).
 *
 * <p>El color lo define la hoja de estilos ({@code -fx-background-color}),
 * de modo que los iconos siguen al tema claro/oscuro y escalan nítidos a
 * cualquier tamaño. Se prefieren iconos vectoriales a los emojis porque el
 * renderizado de estos depende de las fuentes del sistema y suele fallar
 * (cuadros vacíos) en JavaFX.</p>
 */
public class Icono extends Region {

    /**
     * @param rutaSvg ruta SVG (lienzo 24x24, estilo Material) del icono.
     * @param tamano  lado del cuadrado en píxeles.
     */
    public Icono(String rutaSvg, double tamano) {
        getStyleClass().add("icono");
        setStyle("-fx-shape: \"" + rutaSvg + "\";");
        setPrefSize(tamano, tamano);
        setMinSize(tamano, tamano);
        setMaxSize(tamano, tamano);
    }
}
