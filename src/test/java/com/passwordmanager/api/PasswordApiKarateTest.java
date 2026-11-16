package com.passwordmanager.api;

import io.karatelabs.core.Runner;
import io.karatelabs.core.SuiteResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Levanta la API REST en un puerto libre y ejecuta contra ella por HTTP
 * todos los escenarios de Karate definidos en {@code src/test/resources/karate}.
 */
@SpringBootTest(classes = ApiApplication.class, webEnvironment = WebEnvironment.RANDOM_PORT)
class PasswordApiKarateTest {

    @LocalServerPort
    private int port;

    @Test
    @DisplayName("Debe pasar todos los escenarios de Karate contra la API REST")
    void testApi_KarateScenarios() {
        // Arrange (Organizar) + Act (Actuar)
        SuiteResult result = Runner.path("classpath:karate")
            .systemProperty("api.port", String.valueOf(port))
            .outputDir("build/karate-reports")
            .outputJunitXml(true)
            .parallel(1);

        // Assert (Verificar)
        assertTrue(result.getScenarioCount() > 0, "Karate no encontró ningún escenario para ejecutar");
        assertEquals(0, result.getScenarioFailedCount(), String.join("\n", result.getErrors()));
    }
}
