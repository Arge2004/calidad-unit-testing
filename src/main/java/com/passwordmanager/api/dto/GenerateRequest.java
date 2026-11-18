package com.passwordmanager.api.dto;

/**
 * Opciones para generar una contraseña. Los campos omitidos toman los mismos
 * valores por defecto que la interfaz de escritorio: 12 caracteres y todas las categorías.
 */
public record GenerateRequest(Integer length, Boolean upper, Boolean lower, Boolean digits, Boolean special) {

    public static final int DEFAULT_LENGTH = 12;

    public GenerateRequest {
        length = length != null ? length : DEFAULT_LENGTH;
        upper = upper != null ? upper : Boolean.TRUE;
        lower = lower != null ? lower : Boolean.TRUE;
        digits = digits != null ? digits : Boolean.TRUE;
        special = special != null ? special : Boolean.TRUE;
    }
}
