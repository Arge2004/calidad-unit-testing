package com.passwordmanager.performance;

import com.passwordmanager.model.PasswordGenerator;
import com.passwordmanager.model.PasswordValidator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PasswordPerformanceTest {

    @Test
    @DisplayName("Debe generar una contraseña individual en menos de 5 milisegundos")
    void testSingleGenerationPerformance() {
        // Arrange (Organizar)
        PasswordGenerator generator = new PasswordGenerator();
        int longitud = 16;
        long limiteMilisegundos = 5;

        // Act (Actuar)
        long inicio = System.nanoTime();
        String contrasena = generator.generate(longitud, true, true, true, true);
        long fin = System.nanoTime();
        
        long duracionMilisegundos = (fin - inicio) / 1_000_000;

        // Assert (Verificar)
        assertNotNull(contrasena);
        assertTrue(duracionMilisegundos < limiteMilisegundos, 
            "La generación tomó demasiado tiempo: " + duracionMilisegundos + " ms (límite: " + limiteMilisegundos + " ms)");
    }

    @Test
    @DisplayName("Debe generar y validar 10,000 contraseñas en menos de 500 milisegundos")
    void testBulkValidationAndGenerationPerformance() {
        // Arrange (Organizar)
        PasswordGenerator generator = new PasswordGenerator();
        PasswordValidator validator = new PasswordValidator();
        int iteraciones = 10_000;
        int longitud = 12;
        long limiteMilisegundos = 500;

        // Act (Actuar)
        long inicio = System.nanoTime();
        for (int i = 0; i < iteraciones; i++) {
            // Genera contraseña usando criterios variados
            String contrasena = generator.generate(longitud, true, true, true, true);
            // Valida la contraseña generada inmediatamente
            PasswordValidator.ValidationResult result = validator.validate(contrasena);
            
            // Verificación interna rápida para asegurar que no se generan datos corruptos
            assertNotNull(contrasena);
            assertEquals(PasswordValidator.Strength.FUERTE, result.getStrength());
        }
        long fin = System.nanoTime();

        long duracionMilisegundos = (fin - inicio) / 1_000_000;

        // Assert (Verificar)
        System.out.println("Rendimiento: Generadas y validadas " + iteraciones + " contraseñas en " + duracionMilisegundos + " ms");
        assertTrue(duracionMilisegundos < limiteMilisegundos, 
            "El procesamiento por lotes tomó demasiado tiempo: " + duracionMilisegundos + " ms (límite: " + limiteMilisegundos + " ms)");
    }
}
