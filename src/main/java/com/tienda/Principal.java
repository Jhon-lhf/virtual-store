package com.tienda;

import javafx.application.Application;

/**
 * Lanzador para ejecutar desde el IDE: al no extender {@code Application},
 * la JVM no exige que JavaFX esté en el module path y la aplicación arranca
 * con los jars en el classpath.
 *
 * <p>Nota: en ese modo JavaFX registra la advertencia "Unsupported JavaFX
 * configuration" (cosmética). Para ejecutar sin advertencias usar
 * {@code mvn clean javafx:run} o la configuración compartida de IntelliJ
 * en {@code .run/}.</p>
 */
public class Principal {

    public static void main(String[] args) {
        Application.launch(AppTienda.class, args);
    }
}
