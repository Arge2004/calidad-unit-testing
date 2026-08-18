package com.passwordmanager.controller;

import com.passwordmanager.model.PasswordGenerator;
import com.passwordmanager.model.PasswordValidator;
import com.passwordmanager.view.PasswordView;
import javafx.event.ActionEvent;

import java.util.List;

/**
 * Controlador que coordina la comunicación entre la vista (PasswordView)
 * y los modelos de generación y validación de contraseñas utilizando JavaFX.
 */
public class PasswordController {

    private final PasswordView view;
    private final PasswordGenerator generator;
    private final PasswordValidator validator;

    public PasswordController(PasswordView view, PasswordGenerator generator, PasswordValidator validator) {
        this.view = view;
        this.generator = generator;
        this.validator = validator;

        // Registrar los manejadores de eventos con expresiones Lambda
        this.view.setOnGenerateAction(this::handleGenerate);
        this.view.setOnValidateAction(this::handleValidate);
    }

    /**
     * Procesa la generación de contraseñas.
     */
    private void handleGenerate(ActionEvent event) {
        try {
            int length = view.getPasswordLength();
            boolean useUpper = view.isUpperSelected();
            boolean useLower = view.isLowerSelected();
            boolean useDigits = view.isDigitsSelected();
            boolean useSpecial = view.isSpecialSelected();

            // Llamar al modelo para generar
            String password = generator.generate(length, useUpper, useLower, useDigits, useSpecial);
            
            // Actualizar vista
            view.setPasswordText(password);
            
            // Validar automáticamente la contraseña recién generada
            runValidation(password);

        } catch (IllegalArgumentException ex) {
            view.showError(ex.getMessage());
        } catch (Exception ex) {
            view.showError("Ha ocurrido un error al generar la contraseña: " + ex.getMessage());
        }
    }

    /**
     * Procesa la validación de contraseñas.
     */
    private void handleValidate(ActionEvent event) {
        String password = view.getPasswordText();
        runValidation(password);
    }

    /**
     * Ejecuta la lógica de validación y mide el rendimiento del procesamiento.
     */
    private void runValidation(String password) {
        // Medir tiempo de inicio en nanosegundos
        long startTime = System.nanoTime();
        
        // Ejecutar validación
        PasswordValidator.ValidationResult result = validator.validate(password);
        
        // Medir tiempo de fin
        long endTime = System.nanoTime();
        
        // Calcular tiempo en milisegundos
        double elapsedMs = (endTime - startTime) / 1_000_000.0;

        // Determinar color hexadecimal, barra de progreso y etiqueta
        String strengthText;
        String hexColor;
        double progress = result.getScore() / 5.0;

        switch (result.getStrength()) {
            case DEBIL:
                hexColor = "#ff2e63"; // Rojo brillante
                strengthText = "DÉBIL";
                break;
            case MEDIA:
                hexColor = "#ff9f43"; // Naranja cálido
                strengthText = "MEDIA";
                break;
            case FUERTE:
                hexColor = "#21bf73"; // Verde esmeralda
                strengthText = "FUERTE";
                break;
            default:
                hexColor = "#8a8a93";
                strengthText = "DESCONOCIDA";
        }

        // Actualizar la interfaz de usuario con los resultados
        view.showStrengthResult(strengthText, hexColor, progress, result.getFeedback(), elapsedMs);
    }
}
