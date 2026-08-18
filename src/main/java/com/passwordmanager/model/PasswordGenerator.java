package com.passwordmanager.model;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Modelo encargado de la generación de contraseñas seguras
 * basadas en diferentes criterios de selección.
 */
public class PasswordGenerator {

    private static final String LOWERCASE = "abcdefghijklmnopqrstuvwxyz";
    private static final String UPPERCASE = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String DIGITS = "0123456789";
    private static final String SPECIAL = "!@#$%^&*()-_=+[]{}|;:,.<>?";

    private final SecureRandom random = new SecureRandom();

    /**
     * Genera una contraseña con las opciones especificadas.
     *
     * @param length Longitud deseada de la contraseña.
     * @param useUpper Incluir mayúsculas.
     * @param useLower Incluir minúsculas.
     * @param useDigits Incluir números.
     * @param useSpecial Incluir caracteres especiales.
     * @return Contraseña generada.
     * @throws IllegalArgumentException Si la longitud es inválida o no se selecciona ninguna categoría.
     */
    public String generate(int length, boolean useUpper, boolean useLower, boolean useDigits, boolean useSpecial) {
        if (length <= 0) {
            throw new IllegalArgumentException("La longitud de la contraseña debe ser mayor que cero.");
        }
        if (!useUpper && !useLower && !useDigits && !useSpecial) {
            throw new IllegalArgumentException("Se debe seleccionar al menos una categoría de caracteres.");
        }

        StringBuilder pool = new StringBuilder();
        List<Character> guaranteedChars = new ArrayList<>();

        if (useUpper) {
            pool.append(UPPERCASE);
            guaranteedChars.add(UPPERCASE.charAt(random.nextInt(UPPERCASE.length())));
        }
        if (useLower) {
            pool.append(LOWERCASE);
            guaranteedChars.add(LOWERCASE.charAt(random.nextInt(LOWERCASE.length())));
        }
        if (useDigits) {
            pool.append(DIGITS);
            guaranteedChars.add(DIGITS.charAt(random.nextInt(DIGITS.length())));
        }
        if (useSpecial) {
            pool.append(SPECIAL);
            guaranteedChars.add(SPECIAL.charAt(random.nextInt(SPECIAL.length())));
        }

        // Si la longitud es menor que el número de categorías seleccionadas,
        // ajustamos los caracteres garantizados para que no excedan el tamaño solicitado
        if (length < guaranteedChars.size()) {
            guaranteedChars = guaranteedChars.subList(0, length);
        }

        List<Character> passwordChars = new ArrayList<>(guaranteedChars);
        String poolStr = pool.toString();

        // Rellenar el resto de la contraseña
        while (passwordChars.size() < length) {
            passwordChars.add(poolStr.charAt(random.nextInt(poolStr.length())));
        }

        // Mezclar los caracteres para evitar patrones predecibles (ej. que empiece siempre con mayúscula)
        Collections.shuffle(passwordChars, random);

        StringBuilder password = new StringBuilder();
        for (char c : passwordChars) {
            password.append(c);
        }

        return password.toString();
    }
}
