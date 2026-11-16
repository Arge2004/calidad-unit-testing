package com.passwordmanager.api;

import com.passwordmanager.api.dto.GenerateRequest;
import com.passwordmanager.api.dto.GenerateResponse;
import com.passwordmanager.api.dto.ValidateRequest;
import com.passwordmanager.api.dto.ValidateResponse;
import com.passwordmanager.model.PasswordGenerator;
import com.passwordmanager.model.PasswordValidator;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Endpoints REST para generar y validar contraseñas.
 */
@RestController
@RequestMapping("/api/passwords")
public class PasswordApiController {

    /** Límite propio de la API para evitar peticiones que agoten la memoria del servidor. */
    static final int MAX_LENGTH = 128;

    private final PasswordGenerator generator;
    private final PasswordValidator validator;

    public PasswordApiController(PasswordGenerator generator, PasswordValidator validator) {
        this.generator = generator;
        this.validator = validator;
    }

    /**
     * Genera una contraseña y devuelve también su evaluación de fortaleza.
     */
    @PostMapping("/generate")
    public GenerateResponse generate(@RequestBody GenerateRequest request) {
        if (request.length() > MAX_LENGTH) {
            throw new IllegalArgumentException(
                "La longitud de la contraseña no puede superar " + MAX_LENGTH + " caracteres.");
        }

        String password = generator.generate(
            request.length(), request.upper(), request.lower(), request.digits(), request.special());
        PasswordValidator.ValidationResult result = validator.validate(password);

        return new GenerateResponse(password, result.getStrength(), result.getScore());
    }

    /**
     * Evalúa la fortaleza de una contraseña. Se usa POST para que la contraseña
     * viaje en el cuerpo y no quede registrada en la URL.
     */
    @PostMapping("/validate")
    public ValidateResponse validate(@RequestBody ValidateRequest request) {
        PasswordValidator.ValidationResult result = validator.validate(request.password());
        return new ValidateResponse(result.getStrength(), result.getScore(), result.getFeedback());
    }
}
