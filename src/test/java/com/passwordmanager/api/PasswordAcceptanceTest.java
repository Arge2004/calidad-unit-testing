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
 * Pruebas de aceptación: levanta la API REST en un puerto libre y ejecuta contra ella
 * por HTTP los escenarios de {@code src/test/resources/karate/aceptacion}.
 *
 * <p>Cada archivo {@code .feature} corresponde a una historia de usuario ({@code @HU-xx})
 * y cada escenario a uno de sus criterios de aceptación ({@code @CA-xx.y}),
 * tal como se definen en {@code docs/historias-de-usuario.md}.
 */
@SpringBootTest(classes = ApiApplication.class, webEnvironment = WebEnvironment.RANDOM_PORT)
class PasswordAcceptanceTest {

    @LocalServerPort
    private int port;

    @Test
    @DisplayName("Debe cumplir todos los criterios de aceptación de las historias de usuario")
    void testAcceptanceCriteria_AllUserStories() {
        // Arrange (Organizar) + Act (Actuar)
        SuiteResult result = Runner.path("classpath:karate/aceptacion")
            .systemProperty("api.port", String.valueOf(port))
            .outputDir("build/karate-reports")
            .outputJunitXml(true)
            .parallel(1);

        // Assert (Verificar)
        assertTrue(result.getScenarioCount() > 0, "Karate no encontró ningún escenario de aceptación");
        assertEquals(0, result.getScenarioFailedCount(), String.join("\n", result.getErrors()));
    }
}
