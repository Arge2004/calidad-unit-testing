package com.passwordmanager;

import com.passwordmanager.controller.PasswordController;
import com.passwordmanager.model.PasswordGenerator;
import com.passwordmanager.model.PasswordValidator;
import com.passwordmanager.view.PasswordView;
import javafx.application.Application;
import javafx.stage.Stage;

/**
 * Clase principal que inicializa y lanza el ciclo de vida de la aplicación JavaFX.
 */
public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        // Inicializar Modelos
        PasswordGenerator generator = new PasswordGenerator();
        PasswordValidator validator = new PasswordValidator();

        // Inicializar Vista con el Stage primario
        PasswordView view = new PasswordView(primaryStage);

        // Inicializar el Controlador para vincular Vista y Modelos
        new PasswordController(view, generator, validator);

        // Mostrar la ventana
        view.show();
    }

    public static void main(String[] args) {
        // Lanzar la aplicación JavaFX
        launch(args);
    }
}
