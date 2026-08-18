package com.passwordmanager.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PasswordValidatorTest {

    @Test
    @DisplayName("Debe validar como DÉBIL una contraseña vacía o nula")
    void testValidate_EmptyPassword() {
        // Arrange (Organizar)
        PasswordValidator validator = new PasswordValidator();
        String contrasenaVacia = "";

        // Act (Actuar)
        PasswordValidator.ValidationResult result = validator.validate(contrasenaVacia);

        // Assert (Verificar)
        assertEquals(PasswordValidator.Strength.DEBIL, result.getStrength());
        assertEquals(0, result.getScore());
        assertTrue(result.getFeedback().contains("La contraseña no puede estar vacía."));
    }

    @Test
    @DisplayName("Debe validar como DÉBIL una contraseña con longitud menor a 8 caracteres")
    void testValidate_WeakPassword_Short() {
        // Arrange (Organizar)
        PasswordValidator validator = new PasswordValidator();
        String contrasenaCorta = "Ab1!"; // Cumple todas las reglas excepto longitud

        // Act (Actuar)
        PasswordValidator.ValidationResult result = validator.validate(contrasenaCorta);

        // Assert (Verificar)
        assertEquals(PasswordValidator.Strength.DEBIL, result.getStrength());
        assertTrue(result.getScore() <= 4); // Su puntaje máximo sería 4 porque no tiene longitud >= 8
        assertTrue(result.getFeedback().contains("Debe tener al menos 8 caracteres."));
    }

    @Test
    @DisplayName("Debe validar como MEDIA una contraseña con longitud suficiente pero sin caracteres especiales ni números")
    void testValidate_MediumPassword_NoSpecialCharsOrNumbers() {
        // Arrange (Organizar)
        PasswordValidator validator = new PasswordValidator();
        String contrasenaMedia = "SoloLetrasMayusculaYMinuscula"; // longitud >= 8, uppercase, lowercase. No digits, no special.

        // Act (Actuar)
        PasswordValidator.ValidationResult result = validator.validate(contrasenaMedia);

        // Assert (Verificar)
        assertEquals(PasswordValidator.Strength.MEDIA, result.getStrength());
        assertEquals(3, result.getScore()); // 3 reglas cumplidas (longitud, mayúscula, minúscula)
        assertTrue(result.getFeedback().contains("Debe contener al menos un número."));
        assertTrue(result.getFeedback().contains("Debe contener al menos un carácter especial (ej. !, @, #, $, etc.)."));
    }

    @Test
    @DisplayName("Debe validar como FUERTE una contraseña que cumple todas las reglas")
    void testValidate_StrongPassword_AllRulesMet() {
        // Arrange (Organizar)
        PasswordValidator validator = new PasswordValidator();
        String contrasenaFuerte = "Segura123!"; // >=8 chars, upper, lower, digit, special

        // Act (Actuar)
        PasswordValidator.ValidationResult result = validator.validate(contrasenaFuerte);

        // Assert (Verificar)
        assertEquals(PasswordValidator.Strength.FUERTE, result.getStrength());
        assertEquals(5, result.getScore());
        assertTrue(result.getFeedback().isEmpty(), "La retroalimentación debería estar vacía para una contraseña fuerte.");
    }
}
