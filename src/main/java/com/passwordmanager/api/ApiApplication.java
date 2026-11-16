package com.passwordmanager.api;

import com.passwordmanager.model.PasswordGenerator;
import com.passwordmanager.model.PasswordValidator;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

/**
 * Punto de entrada de la API REST. Expone por HTTP los mismos modelos
 * que usa la aplicación de escritorio JavaFX (ver {@code Main}).
 */
@SpringBootApplication
public class ApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiApplication.class, args);
    }

    @Bean
    PasswordGenerator passwordGenerator() {
        return new PasswordGenerator();
    }

    @Bean
    PasswordValidator passwordValidator() {
        return new PasswordValidator();
    }
}
