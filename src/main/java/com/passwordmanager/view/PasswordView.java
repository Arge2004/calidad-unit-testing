package com.passwordmanager.view;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;
import java.util.List;

/**
 * Vista JavaFX para la aplicación de gestión de contraseñas.
 * Proporciona una interfaz gráfica moderna y estilizada.
 */
public class PasswordView {

    private final Stage stage;

    // Controles de Generación
    private Slider sliderLength;
    private Label lblLengthValue;
    private CheckBox chkUpper;
    private CheckBox chkLower;
    private CheckBox chkDigits;
    private CheckBox chkSpecial;
    private Button btnGenerate;

    // Controles de Validación
    private TextField txtPassword;
    private Button btnValidate;
    private Label lblStrengthValue;
    private ProgressBar progressStrength;
    private TextArea txtFeedback;
    private Label lblTimeValue;

    public PasswordView(Stage stage) {
        this.stage = stage;
        initUI();
    }

    private void initUI() {
        stage.setTitle("Generador & Validador de Contraseñas (JavaFX)");

        // Layout Principal: BorderPane
        BorderPane root = new BorderPane();
        root.setPadding(new Insets(20));

        // --- Encabezado ---
        VBox pnlHeader = new VBox(5);
        pnlHeader.setAlignment(Pos.CENTER);
        pnlHeader.setPadding(new Insets(0, 0, 20, 0));

        Label lblTitle = new Label("Generador & Validador de Contraseñas");
        lblTitle.getStyleClass().add("title-label");
        
        Label lblSubtitle = new Label("Estructura MVC y Pruebas Unitarias de Calidad");
        lblSubtitle.setStyle("-fx-text-fill: #8a8a93; -fx-font-size: 12px;");

        pnlHeader.getChildren().addAll(lblTitle, lblSubtitle);
        root.setTop(pnlHeader);

        // --- Cuerpo Central (Dos Columnas) ---
        GridPane centerGrid = new GridPane();
        centerGrid.setHgap(20);
        centerGrid.setVgap(20);

        // Configuración de columnas para redimensionar equitativamente
        ColumnConstraints col1 = new ColumnConstraints();
        col1.setPercentWidth(50);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setPercentWidth(50);
        centerGrid.getColumnConstraints().addAll(col1, col2);

        // Column Left: Generador
        VBox pnlGenerate = new VBox(15);
        pnlGenerate.getStyleClass().add("pane");
        pnlGenerate.setPadding(new Insets(20));

        Label lblGenTitle = new Label("Configurar Generación");
        lblGenTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #00adb5;");

        // Slider para Longitud
        VBox pnlSlider = new VBox(5);
        HBox pnlSliderLabel = new HBox();
        pnlSliderLabel.setAlignment(Pos.CENTER_LEFT);
        Label lblLength = new Label("Longitud: ");
        lblLengthValue = new Label("12");
        lblLengthValue.setStyle("-fx-font-weight: bold; -fx-text-fill: #00adb5;");
        pnlSliderLabel.getChildren().addAll(lblLength, lblLengthValue);

        sliderLength = new Slider(4, 32, 12);
        sliderLength.setBlockIncrement(1);
        sliderLength.setMajorTickUnit(4);
        sliderLength.setMinorTickCount(3);
        sliderLength.setSnapToTicks(true);
        sliderLength.setShowTickMarks(true);
        // Sincronizar etiqueta del slider en tiempo real
        sliderLength.valueProperty().addListener((obs, oldVal, newVal) -> 
            lblLengthValue.setText(String.valueOf(newVal.intValue()))
        );

        pnlSlider.getChildren().addAll(pnlSliderLabel, sliderLength);

        // Opciones de categoría
        chkUpper = new CheckBox("Incluir Mayúsculas (A-Z)");
        chkUpper.setSelected(true);
        chkLower = new CheckBox("Incluir Minúsculas (a-z)");
        chkLower.setSelected(true);
        chkDigits = new CheckBox("Incluir Números (0-9)");
        chkDigits.setSelected(true);
        chkSpecial = new CheckBox("Incluir Especiales (!@#...)");
        chkSpecial.setSelected(true);

        btnGenerate = new Button("Generar Contraseña");
        btnGenerate.setMaxWidth(Double.MAX_VALUE);
        btnGenerate.setStyle("-fx-background-color: #00adb5; -fx-text-fill: #1e1e24;");

        pnlGenerate.getChildren().addAll(lblGenTitle, pnlSlider, chkUpper, chkLower, chkDigits, chkSpecial, btnGenerate);
        centerGrid.add(pnlGenerate, 0, 0);

        // Column Right: Validador
        VBox pnlValidate = new VBox(15);
        pnlValidate.getStyleClass().add("pane");
        pnlValidate.setPadding(new Insets(20));

        Label lblValTitle = new Label("Validación y Fortaleza");
        lblValTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #00adb5;");

        // Entrada de contraseña
        VBox pnlInput = new VBox(5);
        Label lblPass = new Label("Contraseña:");
        txtPassword = new TextField();
        txtPassword.setPromptText("Escribe o genera una contraseña...");
        pnlInput.getChildren().addAll(lblPass, txtPassword);

        // Botón Validar
        btnValidate = new Button("Validar Fortaleza");
        btnValidate.setMaxWidth(Double.MAX_VALUE);
        btnValidate.setStyle("-fx-background-color: #3f3f46; -fx-text-fill: #eeeeee;");

        // Barra de progreso y texto de fortaleza
        VBox pnlStrength = new VBox(8);
        HBox pnlStrengthLabels = new HBox();
        Label lblStrText = new Label("Fortaleza: ");
        lblStrengthValue = new Label("NINGUNA");
        lblStrengthValue.setStyle("-fx-font-weight: bold; -fx-text-fill: #8a8a93;");
        pnlStrengthLabels.getChildren().addAll(lblStrText, lblStrengthValue);

        progressStrength = new ProgressBar(0);
        progressStrength.setMaxWidth(Double.MAX_VALUE);
        progressStrength.setPrefHeight(12);
        progressStrength.setStyle("-fx-accent: #3f3f46;");

        pnlStrength.getChildren().addAll(pnlStrengthLabels, progressStrength);

        // Retroalimentación / Consejos
        VBox pnlFeedback = new VBox(5);
        Label lblFeedbackTitle = new Label("Recomendaciones de seguridad:");
        txtFeedback = new TextArea();
        txtFeedback.setEditable(false);
        txtFeedback.setWrapText(true);
        txtFeedback.setPrefHeight(100);
        pnlFeedback.getChildren().addAll(lblFeedbackTitle, txtFeedback);

        // Tiempo
        HBox pnlTime = new HBox(5);
        Label lblTime = new Label("Tiempo de procesamiento: ");
        lblTimeValue = new Label("-");
        lblTimeValue.setStyle("-fx-text-fill: #00adb5; -fx-font-weight: bold;");
        pnlTime.getChildren().addAll(lblTime, lblTimeValue);

        pnlValidate.getChildren().addAll(lblValTitle, pnlInput, btnValidate, pnlStrength, pnlFeedback, pnlTime);
        centerGrid.add(pnlValidate, 1, 0);

        root.setCenter(centerGrid);

        // --- Definición y Carga de Estilos CSS vía Data URI ---
        String css = "data:text/css," +
                ".root {" +
                "  -fx-background-color: #1e1e24;" +
                "  -fx-font-family: 'Segoe UI', Helvetica, Arial, sans-serif;" +
                "}" +
                ".label {" +
                "  -fx-text-fill: #eeeeee;" +
                "  -fx-font-size: 13px;" +
                "}" +
                ".title-label {" +
                "  -fx-font-size: 22px;" +
                "  -fx-font-weight: bold;" +
                "  -fx-text-fill: #00adb5;" +
                "}" +
                ".pane {" +
                "  -fx-background-color: #2d2d34;" +
                "  -fx-background-radius: 10px;" +
                "  -fx-border-color: #3f3f46;" +
                "  -fx-border-radius: 10px;" +
                "  -fx-border-width: 1px;" +
                "}" +
                ".text-field {" +
                "  -fx-background-color: #1e1e24;" +
                "  -fx-text-fill: #eeeeee;" +
                "  -fx-background-radius: 5px;" +
                "  -fx-border-color: #3f3f46;" +
                "  -fx-border-radius: 5px;" +
                "  -fx-padding: 8px;" +
                "}" +
                ".text-field:focused {" +
                "  -fx-border-color: #00adb5;" +
                "}" +
                ".button {" +
                "  -fx-font-weight: bold;" +
                "  -fx-background-radius: 5px;" +
                "  -fx-padding: 10px 15px;" +
                "  -fx-cursor: hand;" +
                "}" +
                ".button:hover {" +
                "  -fx-opacity: 0.9;" +
                "}" +
                ".check-box {" +
                "  -fx-text-fill: #eeeeee;" +
                "}" +
                ".check-box .box {" +
                "  -fx-background-color: #1e1e24;" +
                "  -fx-border-color: #3f3f46;" +
                "  -fx-border-radius: 3px;" +
                "}" +
                ".check-box:selected .box {" +
                "  -fx-background-color: #00adb5;" +
                "}" +
                ".text-area {" +
                "  -fx-control-inner-background: #1e1e24;" +
                "  -fx-text-fill: #eeeeee;" +
                "  -fx-background-color: transparent;" +
                "  -fx-background-radius: 5px;" +
                "  -fx-border-color: #3f3f46;" +
                "  -fx-border-radius: 5px;" +
                "}" +
                ".text-area .content {" +
                "  -fx-background-color: #1e1e24;" +
                "}" +
                ".progress-bar .track {" +
                "  -fx-background-color: #1e1e24;" +
                "  -fx-background-radius: 5px;" +
                "}" +
                ".progress-bar .bar {" +
                "  -fx-background-radius: 5px;" +
                "}";

        Scene scene = new Scene(root, 720, 520);
        scene.getStylesheets().add(css.replace(" ", "%20")); // Asegurar URL encoding básico para espacios
        stage.setScene(scene);
        stage.setResizable(false);
    }

    public void show() {
        stage.show();
    }

    // --- Métodos de Acceso a Valores ---

    public int getPasswordLength() {
        return (int) sliderLength.getValue();
    }

    public boolean isUpperSelected() {
        return chkUpper.isSelected();
    }

    public boolean isLowerSelected() {
        return chkLower.isSelected();
    }

    public boolean isDigitsSelected() {
        return chkDigits.isSelected();
    }

    public boolean isSpecialSelected() {
        return chkSpecial.isSelected();
    }

    public String getPasswordText() {
        return txtPassword.getText();
    }

    public void setPasswordText(String password) {
        txtPassword.setText(password);
    }

    // --- Métodos para Registrar Listeners ---

    public void setOnGenerateAction(EventHandler<ActionEvent> handler) {
        btnGenerate.setOnAction(handler);
    }

    public void setOnValidateAction(EventHandler<ActionEvent> handler) {
        btnValidate.setOnAction(handler);
    }

    // --- Métodos de Actualización de UI ---

    public void showStrengthResult(String strength, String hexColor, double progress, List<String> feedback, double elapsedMs) {
        lblStrengthValue.setText(strength);
        lblStrengthValue.setStyle("-fx-font-weight: bold; -fx-text-fill: " + hexColor + ";");
        
        progressStrength.setProgress(progress);
        progressStrength.setStyle("-fx-accent: " + hexColor + ";");

        StringBuilder sb = new StringBuilder();
        if (feedback.isEmpty()) {
            sb.append("✓ ¡La contraseña cumple con todas las directrices de seguridad!");
        } else {
            for (String advice : feedback) {
                sb.append("• ").append(advice).append("\n");
            }
        }
        txtFeedback.setText(sb.toString());
        lblTimeValue.setText(String.format("%.4f ms", elapsedMs));
    }

    public void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        
        // Estilizar diálogo de alerta para que combine con el tema oscuro
        DialogPane dialogPane = alert.getDialogPane();
        dialogPane.setStyle("-fx-background-color: #2d2d34;");
        dialogPane.lookup(".content.label").setStyle("-fx-text-fill: #eeeeee;");
        dialogPane.lookupButton(ButtonType.OK).setStyle("-fx-background-color: #00adb5; -fx-text-fill: #1e1e24; -fx-font-weight: bold;");
        
        alert.showAndWait();
    }
}
