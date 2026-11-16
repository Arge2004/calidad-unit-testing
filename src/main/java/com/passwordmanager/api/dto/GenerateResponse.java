package com.passwordmanager.api.dto;

import com.passwordmanager.model.PasswordValidator.Strength;

/**
 * Contraseña generada junto con su evaluación de fortaleza.
 */
public record GenerateResponse(String password, Strength strength, int score) {
}
