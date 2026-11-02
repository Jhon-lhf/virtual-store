package com.tienda.ui;

import com.tienda.modelo.Rol;
import com.tienda.servicio.ExcepcionNegocio;
import com.tienda.servicio.ServicioAuth;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * Diálogo modal de creación de cuenta. El rol elegible es solo
 * {@code CLIENTE} o {@code VENDEDOR}: nadie se autoproclama administrador
 * (eso lo hace un admin desde la pantalla de usuarios).
 */
public class VistaRegistro {

    private final GestorEscenas app;
    private final Runnable alExito;
    private final Stage dialogo = new Stage();
    private final TextField campoUsuario = new TextField();
    private final PasswordField campoClave = new PasswordField();
    private final TextField campoNombre = new TextField();
    private final TextField campoCorreo = new TextField();
    private final ComboBox<String> comboRol = new ComboBox<>();
    private final Label etiquetaError = new Label();

    public VistaRegistro(GestorEscenas app, Runnable alExito) {
        this.app = app;
        this.alExito = alExito;
        construir();
        dialogo.initModality(Modality.APPLICATION_MODAL);
        dialogo.initOwner(app.getEscenario());
        dialogo.setTitle("Crear cuenta");
        dialogo.setResizable(false);
    }

    /** Abre el diálogo y espera hasta que se cierre. */
    public void mostrar() {
        dialogo.showAndWait();
    }

    private void construir() {
        VBox tarjeta = new VBox(14);
        tarjeta.getStyleClass().add("panel");
        tarjeta.setMaxWidth(430);
        tarjeta.setPadding(new Insets(28));

        Label titulo = new Label("Crear cuenta");
        titulo.getStyleClass().add("titulo-panel");
        Label subtitulo = new Label("Regístrate como cliente o vendedor");
        subtitulo.getStyleClass().add("texto-apagado");

        GridPane formulario = new GridPane();
        formulario.setHgap(12);
        formulario.setVgap(12);
        ColumnConstraints columna = new ColumnConstraints();
        columna.setHgrow(Priority.ALWAYS);
        formulario.getColumnConstraints().add(columna);

        campoUsuario.getStyleClass().add("campo");
        campoUsuario.setPromptText("Tu nombre de usuario");
        campoClave.getStyleClass().add("campo");
        campoClave.setPromptText("Mínimo 4 caracteres");
        campoNombre.getStyleClass().add("campo");
        campoNombre.setPromptText("Nombre y apellido");
        campoCorreo.getStyleClass().add("campo");
        campoCorreo.setPromptText("correo@ejemplo.com");
        comboRol.getStyleClass().add("campo");
        comboRol.getItems().addAll("Cliente", "Vendedor");
        comboRol.setValue("Cliente");

        String[][] filas = {
                {"Usuario", "usuario"},
                {"Clave", "clave"},
                {"Nombre", "nombre"},
                {"Correo", "correo"},
                {"Rol", "rol"}
        };
        int fila = 0;
        for (String[] definicion : filas) {
            Label etiqueta = new Label(definicion[0]);
            etiqueta.getStyleClass().add("etiqueta");
            formulario.add(etiqueta, 0, fila++);
            switch (definicion[1]) {
                case "usuario" -> formulario.add(campoUsuario, 0, fila++);
                case "clave" -> formulario.add(campoClave, 0, fila++);
                case "nombre" -> formulario.add(campoNombre, 0, fila++);
                case "correo" -> formulario.add(campoCorreo, 0, fila++);
                default -> formulario.add(comboRol, 0, fila++);
            }
        }

        etiquetaError.getStyleClass().add("texto-error");
        etiquetaError.setWrapText(true);
        etiquetaError.setVisible(false);
        etiquetaError.setManaged(false);

        Button btnRegistrar = new Button("Registrarse");
        btnRegistrar.getStyleClass().addAll("btn", "btn-primario");
        btnRegistrar.setMaxWidth(Double.MAX_VALUE);
        btnRegistrar.setDefaultButton(true);
        btnRegistrar.setOnAction(e -> registrarse());

        Button btnCancelar = new Button("Cancelar");
        btnCancelar.getStyleClass().add("btn");
        btnCancelar.setMaxWidth(Double.MAX_VALUE);
        btnCancelar.setCancelButton(true);
        btnCancelar.setOnAction(e -> dialogo.close());

        tarjeta.getChildren().addAll(titulo, subtitulo, formulario, etiquetaError, btnRegistrar, btnCancelar);

        StackPane raiz = new StackPane(tarjeta);
        raiz.getStyleClass().add("fondo-login");
        raiz.setPadding(new Insets(16));

        Scene escena = new Scene(raiz);
        app.aplicarEstilos(escena);
        dialogo.setScene(escena);
    }

    private void registrarse() {
        try {
            Rol rol = "Vendedor".equals(comboRol.getValue()) ? Rol.VENDEDOR : Rol.CLIENTE;
            app.getServicioAuth().registrar(new ServicioAuth.DatosRegistro(
                    campoUsuario.getText(),
                    campoClave.getText(),
                    campoNombre.getText(),
                    campoCorreo.getText(),
                    rol));
            dialogo.close();
            alExito.run();
        } catch (ExcepcionNegocio e) {
            etiquetaError.setText(e.getMessage());
            etiquetaError.setVisible(true);
            etiquetaError.setManaged(true);
        }
    }
}
