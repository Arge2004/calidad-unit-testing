# Historias de usuario y pruebas de aceptación

## ¿Qué es una prueba de aceptación?

Una **prueba de aceptación** verifica que el sistema cumple lo que el usuario necesita, expresado
como **criterios de aceptación** de una **historia de usuario (HU)**. A diferencia de la prueba
unitaria (que verifica una clase aislada), la prueba de aceptación ejercita el sistema completo
"desde afuera", como lo usaría un cliente, y responde a la pregunta: *¿esta historia está terminada?*

El flujo que seguimos es:

1. **Historia de usuario** con el formato *Como… / Quiero… / Para…*
2. **Criterios de aceptación** escritos en *Dado / Cuando / Entonces* (Given / When / Then).
3. **Escenario ejecutable** en Gherkin (Karate) por cada criterio, etiquetado con `@HU-xx` y `@CA-xx.y`.
4. La prueba levanta la API real (Spring Boot en un puerto libre) y ejecuta los escenarios por HTTP.
   Si todos pasan, la historia se **acepta**.

| Elemento | Ubicación |
|---|---|
| Historias y criterios (este documento) | `docs/historias-de-usuario.md` |
| Escenarios de aceptación (Gherkin/Karate) | `src/test/resources/karate/aceptacion/*.feature` |
| Ejecutor (JUnit + Spring Boot) | `src/test/java/com/passwordmanager/api/PasswordAcceptanceTest.java` |
| Reporte HTML | `build/karate-reports/karate-summary.html` |

**Ejecutar solo las pruebas de aceptación:**

```bash
gradle test --tests '*AcceptanceTest'
```

---

## HU-01 Generar una contraseña segura

> **Como** usuario que crea cuentas en distintos servicios
> **quiero** generar una contraseña aleatoria eligiendo su longitud y los tipos de caracteres
> **para** no tener que inventarla y usar una que sea difícil de adivinar.

Endpoint: `POST /api/passwords/generate` — cuerpo `{ length, upper, lower, digits, special }` (todos opcionales).

| ID | Criterio de aceptación |
|---|---|
| CA-01.1 | **Dado** que no envío opciones, **cuando** pido generar, **entonces** recibo una contraseña de 12 caracteres con las 4 categorías, clasificada como `FUERTE` (puntaje 5). |
| CA-01.2 | **Dado** una longitud entre 1 y 128, **cuando** pido generar, **entonces** la contraseña tiene exactamente esa longitud. |
| CA-01.3 | **Dado** que selecciono una sola categoría, **cuando** pido generar, **entonces** la contraseña solo contiene caracteres de esa categoría. |
| CA-01.4 | **Dado** que selecciono varias categorías, **cuando** pido generar, **entonces** la contraseña incluye al menos un carácter de cada una. |
| CA-01.5 | **Dado** cualquier petición válida, **cuando** se genera la contraseña, **entonces** la respuesta incluye su fortaleza y puntaje (p. ej. 6 caracteres → `DEBIL`). |
| CA-01.6 | **Dado** una longitud menor que 1 o mayor que 128, **cuando** pido generar, **entonces** recibo `400` con un mensaje que explica el error. |
| CA-01.7 | **Dado** que no selecciono ninguna categoría, **cuando** pido generar, **entonces** recibo `400` con el mensaje *"Se debe seleccionar al menos una categoría de caracteres."* |
| CA-01.8 | **Dado** un cuerpo con tipos de dato incorrectos, **cuando** pido generar, **entonces** recibo `400` con el mensaje *"El cuerpo de la petición no es un JSON válido."* |

## HU-02 Evaluar la fortaleza de una contraseña

> **Como** usuario que ya tiene una contraseña
> **quiero** saber qué tan fuerte es y qué le falta
> **para** poder mejorarla antes de usarla.

Endpoint: `POST /api/passwords/validate` — cuerpo `{ password }`.

Reglas (1 punto cada una): longitud ≥ 8, mayúscula, minúscula, número y carácter especial.
Clasificación: `FUERTE` = 5 puntos; `MEDIA` = 3–4 puntos; `DEBIL` = 0–2 puntos **o** menos de 8 caracteres.

| ID | Criterio de aceptación |
|---|---|
| CA-02.1 | **Dado** una contraseña que cumple las 5 reglas, **cuando** la evalúo, **entonces** es `FUERTE`, puntaje 5 y sin observaciones. |
| CA-02.2 | **Dado** una contraseña de 8 o más caracteres, **cuando** la evalúo, **entonces** su nivel corresponde al puntaje y recibo una observación por cada regla incumplida. |
| CA-02.3 | **Dado** una contraseña de menos de 8 caracteres, **cuando** la evalúo, **entonces** es `DEBIL` aunque cumpla las demás reglas. |
| CA-02.4 | **Dado** una contraseña a la que le falta una regla, **cuando** la evalúo, **entonces** la observación indica exactamente qué falta. |
| CA-02.5 | **Dado** una contraseña vacía, con solo espacios o ausente, **cuando** la evalúo, **entonces** es `DEBIL` con puntaje 0 y el mensaje *"La contraseña no puede estar vacía."* |
| CA-02.6 | **Dado** una petición sin cuerpo, **cuando** la envío, **entonces** recibo `400`. |
| CA-02.7 | **Dado** que intento evaluar con `GET`, **cuando** envío la petición, **entonces** recibo `405`, para que la contraseña nunca viaje en la URL. |

---

## Matriz de trazabilidad

| HU | Criterio | Escenario(s) en Karate | Estado |
|---|---|---|---|
| HU-01 | CA-01.1 | `HU-01_generar_contrasena.feature` → CA-01.1 | ✅ |
| HU-01 | CA-01.2 | CA-01.2 (4 ejemplos: 1, 8, 32, 128) | ✅ |
| HU-01 | CA-01.3 | CA-01.3 (4 ejemplos, uno por categoría) | ✅ |
| HU-01 | CA-01.4 | CA-01.4 | ✅ |
| HU-01 | CA-01.5 | CA-01.5 (2 escenarios) | ✅ |
| HU-01 | CA-01.6 | CA-01.6 (3 ejemplos: 0, -5, 129) | ✅ |
| HU-01 | CA-01.7 | CA-01.7 | ✅ |
| HU-01 | CA-01.8 | CA-01.8 | ✅ |
| HU-02 | CA-02.1 | `HU-02_evaluar_fortaleza.feature` → CA-02.1 | ✅ |
| HU-02 | CA-02.2 | CA-02.2 (4 ejemplos) | ✅ |
| HU-02 | CA-02.3 | CA-02.3 | ✅ |
| HU-02 | CA-02.4 | CA-02.4 | ✅ |
| HU-02 | CA-02.5 | CA-02.5 (3 ejemplos) | ✅ |
| HU-02 | CA-02.6 | CA-02.6 | ✅ |
| HU-02 | CA-02.7 | CA-02.7 | ✅ |

**Total:** 2 historias, 15 criterios, 29 ejecuciones de escenario (17 + 12), todas aprobadas.

## Pirámide de pruebas del proyecto

| Nivel | Herramienta | Clase / archivo | Qué verifica |
|---|---|---|---|
| Unitarias | JUnit 6 | `PasswordGeneratorTest`, `PasswordValidatorTest` | Cada regla del modelo aislada |
| Rendimiento | JUnit 6 | `PasswordPerformanceTest` | Tiempos máximos de generación y validación |
| **Aceptación** | **Karate + Spring Boot** | **`PasswordAcceptanceTest`** | **Criterios de aceptación de HU-01 y HU-02 sobre la API real** |
| Calidad estática | SonarCloud + JaCoCo | `build.gradle`, `.github/workflows/quality.yml` | Deuda técnica, complejidad, code smells, cobertura |
