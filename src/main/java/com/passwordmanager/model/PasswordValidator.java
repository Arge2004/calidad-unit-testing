package com.passwordmanager.model;
import java.util.regex.Pattern;
import java.util.ArrayList;
import java.util.List;

/**
 * Modelo encargado de evaluar la fortaleza de las contraseñas
 * y proveer retroalimentación de seguridad.
 */
public class PasswordValidator {
    // Cada patrón describe un solo carácter y se busca con find(): evita el backtracking de ".*X.*"
    private static final Pattern MAYUSCULAS =
        Pattern.compile("[A-Z]");

    private static final Pattern MINUSCULAS =
        Pattern.compile("[a-z]");

    private static final Pattern NUMEROS =
        Pattern.compile("\\d");

    private static final Pattern ESPECIALES =
        Pattern.compile("[!@#$%^&*()\\-_=+\\[\\]{}|;:,.<>?]");
    public enum Strength {
        DEBIL,
        MEDIA,
        FUERTE
    }

    public static class ValidationResult {
        private final Strength strength;
        private final List<String> feedback;
        private final int score;

        public ValidationResult(Strength strength, List<String> feedback, int score) {
            this.strength = strength;
            this.feedback = feedback;
            this.score = score;
        }

        public Strength getStrength() {
            return strength;
        }

        public List<String> getFeedback() {
            return feedback;
        }

        public int getScore() {
            return score;
        }

        @Override
        public String toString() {
            return "Fortaleza: " + strength + " (Score: " + score + "/5) - Retroalimentación: " + feedback;
        }
    }

    /**
     * Valida la fortaleza de una contraseña y proporciona retroalimentación.
     *
     * @param password Contraseña a validar.
     * @return ValidationResult con el puntaje, nivel de fortaleza y retroalimentación detallada.
     */
    public ValidationResult validate(String password) {
        List<String> feedback = new ArrayList<>();
        
        if (password == null || password.trim().isEmpty()) {
            feedback.add("La contraseña no puede estar vacía.");
            return new ValidationResult(Strength.DEBIL, feedback, 0);
        }

        int score = 0;

        // Regla 1: Longitud
        if (password.length() >= 8) {
            score++;
        } else {
            feedback.add("Debe tener al menos 8 caracteres.");
        }

        // Regla 2: Letras Mayúsculas
        if (MAYUSCULAS.matcher(password).find()) {
            score++;
        } else {
            feedback.add("Debe contener al menos una letra mayúscula.");
        }

        // Regla 3: Letras Minúsculas
        if (MINUSCULAS.matcher(password).find()) {
            score++;
        } else {
            feedback.add("Debe contener al menos una letra minúscula.");
        }

        // Regla 4: Números
        if (NUMEROS.matcher(password).find()) {
            score++;
        } else {
            feedback.add("Debe contener al menos un número.");
        }

        // Regla 5: Caracteres Especiales
        // Usamos una expresión regular que busca símbolos comunes
        if (ESPECIALES.matcher(password).find()) {
            score++;
        } else {
            feedback.add("Debe contener al menos un carácter especial (ej. !, @, #, $, etc.).");
        }

        return new ValidationResult(classify(password.length(), score), feedback, score);
    }

    /**
     * Clasifica la fortaleza según el puntaje. Las contraseñas de menos de 8 caracteres
     * son siempre DÉBILES, sin importar cuántas reglas más cumplan.
     */
    private static Strength classify(int length, int score) {
        if (length < 8 || score <= 2) {
            return Strength.DEBIL;
        }
        return score <= 4 ? Strength.MEDIA : Strength.FUERTE;
    }
}
