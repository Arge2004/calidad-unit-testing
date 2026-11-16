package com.passwordmanager.api;

import com.passwordmanager.api.dto.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Traduce los errores de entrada a respuestas 400 con un mensaje legible.
 */
@RestControllerAdvice
public class ApiExceptionHandler {

    /** Reglas de negocio incumplidas (longitud inválida, ninguna categoría seleccionada, etc.). */
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleInvalidArgument(IllegalArgumentException ex) {
        return new ErrorResponse(ex.getMessage());
    }

    /** Cuerpo ausente, JSON mal formado o tipos de dato incorrectos. */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleUnreadableBody(HttpMessageNotReadableException ex) {
        return new ErrorResponse("El cuerpo de la petición no es un JSON válido.");
    }
}
