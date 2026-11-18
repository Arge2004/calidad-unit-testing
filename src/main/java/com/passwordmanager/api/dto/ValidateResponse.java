package com.passwordmanager.api.dto;

import com.passwordmanager.model.PasswordValidator.Strength;

import java.util.List;

/**
 * Resultado de evaluar una contraseña: nivel, puntaje (0-5) y reglas incumplidas.
 */
public record ValidateResponse(Strength strength, int score, List<String> feedback) {
}
