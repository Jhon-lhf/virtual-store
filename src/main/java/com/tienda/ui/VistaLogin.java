package com.tienda.ui;

import com.tienda.modelo.Usuario;
import com.tienda.servicio.ExcepcionNegocio;
import javafx.geometry.HPos;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

/**
 * Pantalla de inicio de sesión: identifica al usuario, muestra el rol con el
 * que entra y da acceso al registro. Incluye las cuentas demo con
 * autocompletado con un clic.
 */
public class VistaLogin {

    private final GestorEscenas app;
    private final StackPane raiz = new StackPane();
    private final TextField campoUsuario = new TextField();
    private final PasswordField campoClave = new PasswordField();
    private final Label etiquetaError = new Label();

    public VistaLogin(GestorEscenas app) {
        this.app = app;
        construir();
    }

    public StackPane getRaiz() {
        return raiz;
    }

    private void construir() {
        raiz.getStyleClass().add("fondo-login");

        VBox tarjeta = new VBox(14);
        tarjeta.getStyleClass().add("panel");
        tarjeta.setMaxWidth(430);
        tarjeta.setPadding(new Insets(32));

        // --- Marca ---
        VBox marca = new VBox(8);
        marca.setAlignment(Pos.CENTER);
        marca.getChildren().addAll(crearLogo(), new Label("Tienda Virtual"));
        marca.getChildren().get(1).getStyleClass().add("marca-grande");
        Label lema = new Label("Todo lo que buscas, en un solo lugar");
        lema.getStyleClass().add("texto-apagado");
        marca.getChildren().add(lema);

        // --- Formulario ---
        GridPane formulario = new GridPane();
        formulario.setHgap(12);
        formulario.setVgap(12);
        ColumnConstraints columna = new ColumnConstraints();
        columna.setHgrow(Priority.ALWAYS);
        formulario.getColumnConstraints().add(columna);

        Label etiquetaUsuario = new Label("Usuario");
        etiquetaUsuario.getStyleClass().add("etiqueta");
        campoUsuario.getStyleClass().add("campo");
        campoUsuario.setPromptText("admin, cliente, vendedor…");

        Label etiquetaClave = new Label("Clave");
        etiquetaClave.getStyleClass().add("etiqueta");
        campoClave.getStyleClass().add("campo");
        campoClave.setPromptText("••••••••");

        formulario.add(etiquetaUsuario, 0, 0);
        formulario.add(campoUsuario, 0, 1);
        formulario.add(etiquetaClave, 0, 2);
        formulario.add(campoClave, 0, 3);

        etiquetaError.getStyleClass().add("texto-error");
        etiquetaError.setWrapText(true);
        etiquetaError.setVisible(false);
        etiquetaError.setManaged(false);

        Button btnEntrar = new Button("Iniciar sesión");
        btnEntrar.getStyleClass().addAll("btn", "btn-primario");
        btnEntrar.setMaxWidth(Double.MAX_VALUE);
        btnEntrar.setDefaultButton(true);
        btnEntrar.setOnAction(e -> iniciarSesion());

        Button btnRegistrar = new Button("Crear una cuenta");
        btnRegistrar.getStyleClass().add("btn");
        btnRegistrar.setMaxWidth(Double.MAX_VALUE);
        btnRegistrar.setOnAction(e -> abrirRegistro());

        campoClave.setOnAction(e -> iniciarSesion());

        // --- Cuentas demo ---
        VBox demo = new VBox(8);
        demo.getStyleClass().add("panel-demo");
        Label tituloDemo = new Label("Cuentas demo");
        tituloDemo.getStyleClass().add("etiqueta");
        HBox chips = new HBox(8);
        chips.setAlignment(Pos.CENTER);
        for (String[] cuenta : new String[][]{{"admin", "admin123"}, {"cliente", "cli123"}, {"vendedor", "ven123"}}) {
            Button chip = new Button(cuenta[0]);
            chip.getStyleClass().add("chip-cuenta");
            String clave = cuenta[1];
            chip.setOnAction(e -> {
                campoUsuario.setText(cuenta[0]);
                campoClave.setText(clave);
                etiquetaError.setVisible(false);
                etiquetaError.setManaged(false);
            });
            chips.getChildren().add(chip);
        }
        Label pistaDemo = new Label("Haz clic en una cuenta para autocompletar");
        pistaDemo.getStyleClass().add("hint");
        demo.getChildren().addAll(tituloDemo, chips, pistaDemo);

        tarjeta.getChildren().addAll(marca, formulario, etiquetaError, btnEntrar, btnRegistrar, demo);
        raiz.getChildren().add(tarjeta);
    }

    private StackPane crearLogo() {
        StackPane logo = new StackPane(new Label("T"));
        logo.getStyleClass().add("marca-logo");
        StackPane.setAlignment(logo.getChildren().get(0), Pos.CENTER);
        return logo;
    }

    private void iniciarSesion() {
        try {
            Usuario usuario = app.getServicioAuth().iniciarSesion(campoUsuario.getText(), campoClave.getText());
            app.getSesion().setUsuario(usuario);
            app.mostrarPrincipal();
        } catch (ExcepcionNegocio e) {
            etiquetaError.setText(e.getMessage());
            etiquetaError.setVisible(true);
            etiquetaError.setManaged(true);
        }
    }

    private void abrirRegistro() {
        VistaRegistro dialogo = new VistaRegistro(app, () ->
                Avisos.mostrar(raiz, Avisos.Tipo.EXITO, "¡Cuenta creada! Ahora inicia sesión"));
        dialogo.mostrar();
    }
}
