package com.passwordmanager.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PasswordGeneratorTest {

    @Test
    @DisplayName("Debe generar una contraseña con la longitud exacta especificada")
    void testGenerate_CorrectLength() {
        // Arrange (Organizar)
        PasswordGenerator generator = new PasswordGenerator();
        int longitudEsperada = 16;

        // Act (Actuar)
        String contrasena = generator.generate(longitudEsperada, true, true, true, true);

        // Assert (Verificar)
        assertNotNull(contrasena);
        assertEquals(longitudEsperada, contrasena.length());
    }

    @Test
    @DisplayName("Debe contener únicamente letras mayúsculas cuando solo se selecciona esa opción")
    void testGenerate_OnlyUppercase() {
        // Arrange (Organizar)
        PasswordGenerator generator = new PasswordGenerator();
        int longitud = 10;

        // Act (Actuar)
        String contrasena = generator.generate(longitud, true, false, false, false);

        // Assert (Verificar)
        assertNotNull(contrasena);
        assertEquals(longitud, contrasena.length());
        assertTrue(contrasena.matches("[A-Z]+"), "La contraseña '" + contrasena + "' debería contener solo mayúsculas.");
    }

    @Test
    @DisplayName("Debe contener únicamente números cuando solo se selecciona esa opción")
    void testGenerate_OnlyDigits() {
        // Arrange (Organizar)
        PasswordGenerator generator = new PasswordGenerator();
        int longitud = 8;

        // Act (Actuar)
        String contrasena = generator.generate(longitud, false, false, true, false);

        // Assert (Verificar)
        assertNotNull(contrasena);
        assertEquals(longitud, contrasena.length());
        assertTrue(contrasena.matches("[0-9]+"), "La contraseña '" + contrasena + "' debería contener solo números.");
    }

    @Test
    @DisplayName("Debe lanzar IllegalArgumentException si la longitud es cero o negativa")
    void testGenerate_InvalidLength_ThrowsException() {
        // Arrange (Organizar)
        PasswordGenerator generator = new PasswordGenerator();
        int longitudInvalida = 0;

        // Act & Assert (Actuar y Verificar)
        assertThrows(IllegalArgumentException.class, () -> {
            generator.generate(longitudInvalida, true, true, true, true);
        }, "Debería haber lanzado una excepción por longitud inválida.");
    }

    @Test
    @DisplayName("Debe lanzar IllegalArgumentException si no se selecciona ninguna categoría de caracteres")
    void testGenerate_NoCharactersSelected_ThrowsException() {
        // Arrange (Organizar)
        PasswordGenerator generator = new PasswordGenerator();
        int longitud = 12;

        // Act & Assert (Actuar y Verificar)
        assertThrows(IllegalArgumentException.class, () -> {
            generator.generate(longitud, false, false, false, false);
        }, "Debería haber lanzado una excepción al no seleccionar categorías de caracteres.");
    }

    @Test
    @DisplayName("Debe contener al menos un carácter de cada tipo seleccionado cuando la longitud es suficiente")
    void testGenerate_ContainsAllRequestedTypes() {
        // Arrange (Organizar)
        PasswordGenerator generator = new PasswordGenerator();
        int longitud = 4; // Longitud mínima para tener uno de cada uno

        // Act (Actuar)
        String contrasena = generator.generate(longitud, true, true, true, true);

        // Assert (Verificar)
        assertNotNull(contrasena);
        assertTrue(contrasena.matches(".*[A-Z].*"), "Debe contener al menos una mayúscula.");
        assertTrue(contrasena.matches(".*[a-z].*"), "Debe contener al menos una minúscula.");
        assertTrue(contrasena.matches(".*[0-9].*"), "Debe contener al menos un número.");
        assertTrue(contrasena.matches(".*[!@#$%^&*()\\-_=+\\[\\]{}|;:,.<>?].*"), "Debe contener al menos un carácter especial.");
    }
}
