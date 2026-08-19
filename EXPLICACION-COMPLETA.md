# Explicación Completa del Proyecto: Generador y Validador de Contraseñas

## Tabla de Contenidos

1. [La Idea General del Proyecto](#1-la-idea-general-del-proyecto)
2. [El Patrón MVC (Model-View-Controller)](#2-el-patrón-mvc-model-view-controller)
3. [Funcionamiento Detallado de Cada Componente](#3-funcionamiento-detallado-de-cada-componente)
4. [Análisis Detallado de Cada Test](#4-análisis-detallado-de-cada-test)
5. [Verificación de Gradle](#5-verificación-de-gradle)

---

## 1. La Idea General del Proyecto

Este es un **Generador y Validador de Contraseñas** construido como aplicación de escritorio con **JavaFX**. El proyecto tiene dos propósitos principales:

1. **Funcional:** Permitir al usuario generar contraseñas seguras con opciones configurables y validar la fortaleza de contraseñas existentes.
2. **Educativo:** Demostrar la implementación del patrón **MVC (Model-View-Controller)** y la escritura de **pruebas unitarias de calidad** con JUnit 5.

---

## 2. El Patrón MVC (Model-View-Controller)

El patrón MVC separa la aplicación en tres capas independientes:

```
┌─────────────┐      ┌──────────────────┐      ┌─────────────────┐
│    VIEW      │◄────►│   CONTROLLER     │◄────►│     MODEL       │
│ PasswordView │      │PasswordController│      │PasswordGenerator│
│              │      │                  │      │PasswordValidator│
└─────────────┘      └──────────────────┘      └─────────────────┘
   (UI JavaFX)        (Coordina lógica)         (Reglas de negocio)
```

### ¿Cómo influye MVC en el diseño del código?

**Ventaja clave para testing:** Al separar el modelo (lógica pura) de la vista (UI JavaFX), los tests pueden probar el modelo **sin necesitar una pantalla gráfica**. Los tests solo instancian `PasswordGenerator` y `PasswordValidator` directamente — nunca tocan `PasswordView` ni `PasswordController`. Esto es exactamente por qué los tests funcionan en un entorno sin display.

---

## 3. Funcionamiento Detallado de Cada Componente

### 3.1. `Main.java` — Punto de Entrada

```java
public class Main extends Application {
    @Override
    public void start(Stage primaryStage) {
        PasswordGenerator generator = new PasswordGenerator();
        PasswordValidator validator = new PasswordValidator();
        PasswordView view = new PasswordView(primaryStage);
        new PasswordController(view, generator, validator);
        view.show();
    }
}
```

**Flujo de inicialización:**
1. Crea las dos instancias del **Modelo** (`generator`, `validator`)
2. Crea la **Vista** pasándole el `Stage` principal de JavaFX
3. Crea el **Controller**, pasándole la vista y los modelos — esto registra los event handlers
4. Muestra la ventana

El `Controller` actúa como intermediario: la vista nunca habla directamente con el modelo.

---

### 3.2. `PasswordGenerator.java` — Modelo de Generación

**Responsabilidad:** Generar contraseñas aleatorias que cumplan con los criterios del usuario.

**Constantes de caracteres:**

```java
LOWERCASE = "abcdefghijklmnopqrstuvwxyz"
UPPERCASE = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
DIGITS    = "0123456789"
SPECIAL   = "!@#$%^&*()-_=+[]{}|;:,.<>?"
```

**Algoritmo paso a paso de `generate()`:**

1. **Validación de entrada:**
   - Si `length <= 0` → lanza `IllegalArgumentException`
   - Si ninguna categoría está seleccionada → lanza `IllegalArgumentException`

2. **Construcción del pool y garantía de caracteres:**
   - Por cada categoría habilitada, agrega sus caracteres al `pool` general
   - Extrae **un carácter aleatorio** de cada categoría habilitada y lo pone en `guaranteedChars`
   - Si `length < guaranteedChars.size()`, trunca la lista de garantías (no puede garantizar más caracteres que la longitud solicitada)

3. **Relleno:** Llena `passwordChars` primero con los garantizados, luego con caracteres aleatorios del pool hasta alcanzar `length`

4. **Mezcla:** `Collections.shuffle(passwordChars, random)` — esto es crítico porque sin mezclar, la contraseña siempre empezaría con los caracteres garantizados (ej: siempre mayúscula primero), lo cual es predecible

5. **Retorno:** Une los caracteres en un `String`

**Uso de `SecureRandom`:** A diferencia de `Random` normal, `SecureRandom` es criptográficamente seguro, apropiado para generar contraseñas reales.

---

### 3.3. `PasswordValidator.java` — Modelo de Validación

**Responsabilidad:** Evaluar la fortaleza de una contraseña y dar retroalimentación.

**Estructura interna:**

```java
public enum Strength { DEBIL, MEDIA, FUERTE }

public static class ValidationResult {
    private final Strength strength;
    private final List<String> feedback;
    private final int score;
}
```

**Algoritmo de `validate()`:**

1. **Caso vacío:** Si `password` es `null` o solo contiene espacios → retorna `DEBIL` con score 0 y feedback "La contraseña no puede estar vacía."

2. **Scoring (0 a 5 puntos):**

   | Punto | Regla | Condición | Si falla |
   |-------|-------|-----------|----------|
   | +1 | Longitud >= 8 | `password.length() >= 8` | "Debe tener al menos 8 caracteres." |
   | +1 | Mayúscula | `password.matches(".*[A-Z].*")` | "Debe contener al menos una letra mayúscula." |
   | +1 | Minúscula | `password.matches(".*[a-z].*")` | "Debe contener al menos una letra minúscula." |
   | +1 | Número | `password.matches(".*[0-9].*")` | "Debe contener al menos un número." |
   | +1 | Especial | `password.matches(".*[!@#$%^&*()\\-_=+\\[\\]{}|;:,.<>?].*")` | "Debe contener al menos un carácter especial." |

3. **Clasificación de fortaleza:**

   ```
   si password.length() < 8  → DEBIL (siempre, sin importar el score)
   si score <= 2              → DEBIL
   si score <= 4              → MEDIA
   si score = 5               → FUERTE
   ```

**Detalle crítico:** Una contraseña como `"Ab1!"` (4 caracteres) tiene score 4 pero es **DEBIL** porque la longitud < 8 siempre fuerza DEBIL. Esto es un edge case importante que los tests verifican.

---

### 3.4. `PasswordController.java` — El Coordinador

**Responsabilidad:** Conectar la vista con el modelo.

**Registro de handlers:**

```java
this.view.setOnGenerateAction(this::handleGenerate);
this.view.setOnValidateAction(this::handleValidate);
```

Usa **method references** (Java 8+) para registrar callbacks.

**`handleGenerate()`:**
1. Lee configuración de la vista (longitud, checkboxes)
2. Llama a `generator.generate()`
3. Actualiza el campo de texto de la vista con la contraseña generada
4. Ejecuta validación automática sobre la contraseña recién generada
5. Captura excepciones y muestra errores en la vista

**`handleValidate()`:**
1. Lee la contraseña del campo de texto
2. Ejecuta `runValidation()`

**`runValidation()` — El más interesante:**

```java
long startTime = System.nanoTime();
ValidationResult result = validator.validate(password);
long endTime = System.nanoTime();
double elapsedMs = (endTime - startTime) / 1_000_000.0;
```

Mide el tiempo de validación en **nanosegundos** y lo convierte a milisegundos para mostrarlo en la UI. Esto es puramente cosmético pero demuestra la medición de rendimiento.

Luego mapea el `Strength` a colores hexadecimales y actualiza la vista.

---

### 3.5. `PasswordView.java` — La Interfaz Gráfica

**Característica notable:** Todo el CSS está **embebido como data URI** dentro del Java, no en archivos externos:

```java
String css = "data:text/css," +
    ".root { -fx-background-color: #1e1e24; }" +
    // ... más reglas
```

Esto significa que la app es un solo archivo ejecutable sin dependencias de archivos externos de estilo.

**Componentes principales:**
- **Slider** para longitud (4-32, default 12, snap to ticks)
- **4 CheckBoxes** para categorías (todos seleccionados por defecto)
- **Botón "Generar Contraseña"** → llama a `handleGenerate`
- **TextField** para ingresar contraseña manualmente
- **Botón "Validar Fortaleza"** → llama a `handleValidate`
- **ProgressBar** que muestra el score/5
- **TextArea** para feedback/recomendaciones
- **Label** que muestra tiempo de procesamiento

---

## 4. Análisis Detallado de Cada Test

Los tests están en `src/test/java/com/passwordmanager/` y usan **JUnit 5 (Jupiter)**.

### Patrón de los Tests: AAA (Arrange-Act-Assert)

Todos los tests siguen el patrón **AAA** con comentarios en español:

```java
// Arrange (Organizar)  ← Preparar datos y objetos
// Act (Actuar)         ← Ejecutar la operación
// Assert (Verificar)   ← Verificar el resultado
```

**¿Por qué AAA?** Es el patrón estándar para pruebas unitarias porque:
- Separa claramente la preparación, ejecución y verificación
- Facilita leer y mantener los tests
- Hace evidente qué se está probando exactamente

### Convención de Nombres: `test_<Método>_<Escenario>`

Cada test tiene un `@DisplayName` descriptivo en español y un nombre de método que indica qué método se prueba y bajo qué escenario.

---

### 4.1. `PasswordValidatorTest.java` — 4 Tests

#### Test 1: `testValidate_EmptyPassword`

```java
@DisplayName("Debe validar como DÉBIL una contraseña vacía o nula")
void testValidate_EmptyPassword() {
    // Arrange (Organizar)
    PasswordValidator validator = new PasswordValidator();
    String contrasenaVacia = "";

    // Act (Actuar)
    PasswordValidator.ValidationResult result = validator.validate(contrasenaVacia);

    // Assert (Verificar)
    assertEquals(PasswordValidator.Strength.DEBIL, result.getStrength());
    assertEquals(0, result.getScore());
    assertTrue(result.getFeedback().contains("La contraseña no puede estar vacía."));
}
```

**Qué verifica:** Cuando la contraseña es una cadena vacía, el validador debe:
- Retornar fortaleza `DEBIL`
- Score exacto de `0`
- Contener el mensaje de error específico

**Edge case:** Prueba el primer `if` del `validate()` — el caso temprano de retorno antes de calcular nada.

---

#### Test 2: `testValidate_WeakPassword_Short`

```java
@DisplayName("Debe validar como DÉBIL una contraseña con longitud menor a 8 caracteres")
void testValidate_WeakPassword_Short() {
    // Arrange (Organizar)
    PasswordValidator validator = new PasswordValidator();
    String contrasenaCorta = "Ab1!"; // Cumple todas las reglas excepto longitud

    // Act (Actuar)
    PasswordValidator.ValidationResult result = validator.validate(contrasenaCorta);

    // Assert (Verificar)
    assertEquals(PasswordValidator.Strength.DEBIL, result.getStrength());
    assertTrue(result.getScore() <= 4);
    assertTrue(result.getFeedback().contains("Debe tener al menos 8 caracteres."));
}
```

**Qué verifica:** `"Ab1!"` tiene mayúscula, minúscula, número y carácter especial = score 4, pero longitud < 8. Este test valida que **la regla de longitud corta siempre fuerza DEBIL** sin importar el score.

**Por qué es importante:** Es el edge case más crítico del validador. Sin este test, un refactor podría romper la lógica "length < 8 = DEBIL siempre".

**Detalle del assertion `getScore() <= 4`:** En vez de verificar score exacto (= 3 porque falla longitud), usa `<= 4` porque el test solo necesita confirmar que no es score 5 (perfecto). Es un assertion más flexible que permite cambios menores en el scoring.

---

#### Test 3: `testValidate_MediumPassword_NoSpecialCharsOrNumbers`

```java
@DisplayName("Debe validar como MEDIA una contraseña con longitud suficiente pero sin caracteres especiales ni números")
void testValidate_MediumPassword_NoSpecialCharsOrNumbers() {
    // Arrange (Organizar)
    PasswordValidator validator = new PasswordValidator();
    String contrasenaMedia = "SoloLetrasMayusculaYMinuscula";

    // Act (Actuar)
    PasswordValidator.ValidationResult result = validator.validate(contrasenaMedia);

    // Assert (Verificar)
    assertEquals(PasswordValidator.Strength.MEDIA, result.getStrength());
    assertEquals(3, result.getScore()); // 3 reglas cumplidas (longitud, mayúscula, minúscula)
    assertTrue(result.getFeedback().contains("Debe contener al menos un número."));
    assertTrue(result.getFeedback().contains("Debe contener al menos un carácter especial (ej. !, @, #, $, etc.)."));
}
```

**Qué verifica:** `"SoloLetrasMayusculaYMinuscula"` tiene:
- Longitud >= 8 ✓ (+1)
- Mayúsculas ✓ (+1)
- Minúsculas ✓ (+1)
- Sin números ✗ (+0)
- Sin especiales ✗ (+0)
- Total: score 3 → MEDIA

Verifica exactamente **qué mensajes de feedback** se generan cuando faltan reglas específicas.

**Por qué score 3 = MEDIA:** Según las reglas, `score <= 4` es MEDIA, y 3 <= 4. Este test confirma el rango intermedio.

---

#### Test 4: `testValidate_StrongPassword_AllRulesMet`

```java
@DisplayName("Debe validar como FUERTE una contraseña que cumple todas las reglas")
void testValidate_StrongPassword_AllRulesMet() {
    // Arrange (Organizar)
    PasswordValidator validator = new PasswordValidator();
    String contrasenaFuerte = "Segura123!"; // >=8 chars, upper, lower, digit, special

    // Act (Actuar)
    PasswordValidator.ValidationResult result = validator.validate(contrasenaFuerte);

    // Assert (Verificar)
    assertEquals(PasswordValidator.Strength.FUERTE, result.getStrength());
    assertEquals(5, result.getScore());
    assertTrue(result.getFeedback().isEmpty(), "La retroalimentación debería estar vacía para una contraseña fuerte.");
}
```

**Qué verifica:** `"Segura123!"` tiene todo: longitud 10, mayúscula 'S', minúsculas, números '123', especial '!'. Score = 5 → FUERTE.

**Aserción clave:** `feedback.isEmpty()` — una contraseña fuerte no debe tener **ningún** mensaje de retroalimentación. Si hay feedback, significa que alguna regla falló, lo cual sería un bug.

---

### 4.2. `PasswordGeneratorTest.java` — 5 Tests

#### Test 1: `testGenerate_CorrectLength`

```java
@DisplayName("Debe generar una contraseña con la longitud exacta especificada")
void testGenerate_CorrectLength() {
    // Arrange (Organizar)
    PasswordGenerator generator = new PasswordGenerator();
    int longitudEsperada = 16;

    // Act (Actuar)
    String contrasena = generator.generate(longitudEsperada, true, true, true, true);

    // Assert (Verificar)
    assertNotNull(contrasena);
    assertEquals(longitudEsperada, contrasena.length());
}
```

**Qué verifica:** La contraseña generada tiene exactamente la longitud solicitada (16). `assertNotNull` verifica que no se retorne `null`.

---

#### Test 2: `testGenerate_OnlyUppercase`

```java
@DisplayName("Debe contener únicamente letras mayúsculas cuando solo se selecciona esa opción")
void testGenerate_OnlyUppercase() {
    // Arrange (Organizar)
    PasswordGenerator generator = new PasswordGenerator();
    int longitud = 10;

    // Act (Actuar)
    String contrasena = generator.generate(longitud, true, false, false, false);

    // Assert (Verificar)
    assertNotNull(contrasena);
    assertEquals(longitud, contrasena.length());
    assertTrue(contrasena.matches("[A-Z]+"), "La contraseña '" + contrasena + "' debería contener solo mayúsculas.");
}
```

**Qué verifica:** Solo se habilita `useUpper`. La contraseña resultante debe contener **exclusivamente** mayúsculas. El regex `[A-Z]+` asegura que cada carácter sea una mayúscula.

---

#### Test 3: `testGenerate_OnlyDigits`

```java
@DisplayName("Debe contener únicamente números cuando solo se selecciona esa opción")
void testGenerate_OnlyDigits() {
    // Arrange (Organizar)
    PasswordGenerator generator = new PasswordGenerator();
    int longitud = 8;

    // Act (Actuar)
    String contrasena = generator.generate(longitud, false, false, true, false);

    // Assert (Verificar)
    assertNotNull(contrasena);
    assertEquals(longitud, contrasena.length());
    assertTrue(contrasena.matches("[0-9]+"), "La contraseña '" + contrasena + "' debería contener solo números.");
}
```

**Qué verifica:** Solo `useDigits` habilitado. Resultado debe ser puramente numérico (`[0-9]+`).

---

#### Test 4: `testGenerate_InvalidLength_ThrowsException`

```java
@DisplayName("Debe lanzar IllegalArgumentException si la longitud es cero o negativa")
void testGenerate_InvalidLength_ThrowsException() {
    // Arrange (Organizar)
    PasswordGenerator generator = new PasswordGenerator();
    int longitudInvalida = 0;

    // Act & Assert (Actuar y Verificar)
    assertThrows(IllegalArgumentException.class, () -> {
        generator.generate(longitudInvalida, true, true, true, true);
    }, "Debería haber lanzado una excepción por longitud inválida.");
}
```

**Qué verifica:** Longitud 0 debe lanzar `IllegalArgumentException`. `assertThrows` es la forma correcta en JUnit 5 de verificar que se lanza una excepción (en vez de usar `@Test(expected=...)` que es JUnit 4).

---

#### Test 5: `testGenerate_NoCharactersSelected_ThrowsException`

```java
@DisplayName("Debe lanzar IllegalArgumentException si no se selecciona ninguna categoría de caracteres")
void testGenerate_NoCharactersSelected_ThrowsException() {
    // Arrange (Organizar)
    PasswordGenerator generator = new PasswordGenerator();
    int longitud = 12;

    // Act & Assert (Actuar y Verificar)
    assertThrows(IllegalArgumentException.class, () -> {
        generator.generate(longitud, false, false, false, false);
    }, "Debería haber lanzado una excepción al no seleccionar categorías de caracteres.");
}
```

**Qué verifica:** Si todas las categorías son `false`, el generador no puede construir un pool de caracteres → debe lanzar excepción.

---

#### Test 6: `testGenerate_ContainsAllRequestedTypes`

```java
@DisplayName("Debe contener al menos un carácter de cada tipo seleccionado cuando la longitud es suficiente")
void testGenerate_ContainsAllRequestedTypes() {
    // Arrange (Organizar)
    PasswordGenerator generator = new PasswordGenerator();
    int longitud = 4; // Longitud mínima para tener uno de cada uno

    // Act (Actuar)
    String contrasena = generator.generate(longitud, true, true, true, true);

    // Assert (Verificar)
    assertNotNull(contrasena);
    assertTrue(contrasena.matches(".*[A-Z].*"), "Debe contener al menos una mayúscula.");
    assertTrue(contrasena.matches(".*[a-z].*"), "Debe contener al menos una minúscula.");
    assertTrue(contrasena.matches(".*[0-9].*"), "Debe contener al menos un número.");
    assertTrue(contrasena.matches(".*[!@#$%^&*()\\-_=+\\[\\]{}|;:,.<>?].*"), "Debe contener al menos un carácter especial.");
}
```

**Qué verifica:** Con longitud 4 (mínimo para tener uno de cada tipo), la contraseña debe contener **al menos un carácter de cada categoría habilitada**. Esto prueba la lógica de `guaranteedChars` del generador.

**Por qué es importante:** Sin la lógica de caracteres garantizados, podría generar una contraseña como `"AAAA"` (solo mayúsculas) aunque se pidieron todas las categorías.

---

### 4.3. `PasswordPerformanceTest.java` — 2 Tests

#### Test 1: `testSingleGenerationPerformance`

```java
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
```

**Qué verifica:** Generar una contraseña de 16 caracteres con todas las categorías debe tomar **menos de 5ms**. Esto es un test de rendimiento que asegura que el generador es eficiente.

**Por qué 5ms:** Es un umbral conservador. `SecureRandom` y `Collections.shuffle` son operaciones rápidas, pero en máquinas muy lentas o con alta carga podría fallar.

---

#### Test 2: `testBulkValidationAndGenerationPerformance`

```java
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
        String contrasena = generator.generate(longitud, true, true, true, true);
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
```

**Qué verifica:** Generar **y** validar 10,000 contraseñas debe tomar **menos de 500ms** en total. También verifica internamente que cada contraseña generada es FUERTE (todas las categorías habilitadas), lo cual sirve como **smoke test** adicional.

**Razón de ser:** Este test detecta regresiones de rendimiento. Si alguien introduce una operación costosa en el ciclo de generación/validación, este test lo atrapará.

**Advertencia:** Estos tests son **flaky** — en máquinas lentas o en CI con recursos limitados, podrían fallar falsamente. Los umbrales hardcodeados son aproximaciones.

---

## 5. Verificación de Gradle

### ¿Está implementado en Gradle?

**Sí.** El proyecto usa Gradle como sistema de construcción.

**`build.gradle`:**

```groovy
plugins {
    id 'application'                                  // Permite ejecutar la app
    id 'org.openjfx.javafxplugin' version '0.1.0'    // Soporte JavaFX
    id 'eclipse'                                      // Soporte Eclipse
    id 'idea'                                         // Soporte IntelliJ
}

repositories {
    mavenCentral()                                    // Repositorio de dependencias
}

dependencies {
    testImplementation 'org.junit.jupiter:junit-jupiter:5.10.0'   // JUnit 5
    testRuntimeOnly 'org.junit.platform:junit-platform-launcher'   // Launcher para tests
}

javafx {
    version = "17"
    modules = ['javafx.controls', 'javafx.graphics', 'javafx.base']
}

application {
    mainClass = 'com.passwordmanager.Main'
}

tasks.named('test') {
    useJUnitPlatform()                                // Usa el platform launcher de JUnit 5
    testLogging {
        events "passed", "skipped", "failed"          // Muestra resultados
        showStandardStreams = true                     // Muestra stdout/stderr
    }
}
```

**`settings.gradle`:**

```groovy
rootProject.name = 'password-manager'
```

### ¿Qué es Gradle?

Gradle es un **sistema de automatización de construcción** (build system) para proyectos Java (y otros lenguajes). Sus funciones principales:

1. **Gestión de dependencias:** Descarga JUnit 5 y JavaFX plugin desde Maven Central automáticamente
2. **Compilación:** Compila el código Java con las dependencias correctas en el classpath
3. **Ejecución de tests:** `gradle test` ejecuta todos los tests con JUnit Platform
4. **Empaquetamiento:** `gradle build` crea un JAR ejecutable
5. **Ejecución de la app:** `gradle run` lanza la aplicación JavaFX

### Comandos Útiles de Gradle

```bash
# Ejecutar todos los tests
gradle test

# Ejecutar una clase de test específica
gradle test --tests "com.passwordmanager.model.PasswordValidatorTest"

# Ejecutar un solo método de test
gradle test --tests "com.passwordmanager.model.PasswordGeneratorTest.testGenerate_CorrectLength"

# Ejecutar la aplicación
gradle run

# Compilar sin ejecutar tests
gradle build -x test

# Limpiar archivos generados
gradle clean
```

### Nota Importante: No hay Gradle Wrapper

No existe `gradlew` ni `gradlew.bat` en el proyecto. Esto significa que:
- Los desarrolladores deben instalar Gradle 9.x en su sistema manualmente
- No se garantiza la misma versión de Gradle entre desarrolladores
- En un proyecto real, se recomienda generar el wrapper con `gradle wrapper`

---

## Resumen

| Aspecto | Detalle |
|---------|---------|
| **Patrón** | MVC — separa modelo (lógica), vista (UI), controller (coordinación) |
| **Beneficio MVC para tests** | El modelo se testea sin UI, sin display, sin dependencias gráficas |
| **Framework de tests** | JUnit 5 (Jupiter) con `@Test`, `@DisplayName`, `assertEquals`, `assertThrows`, `assertTrue` |
| **Patrón de tests** | AAA (Arrange-Act-Assert) con comentarios en español |
| **Naming** | `test_<Método>_<Escenario>` |
| **Tests de unitarios** | 9 tests que cubren casos normales, edge cases, y excepciones |
| **Tests de rendimiento** | 2 tests con umbrales hardcodeados (5ms individual, 500ms para 10k) |
| **Build system** | Gradle 9.2.0 con plugins de JavaFX y JUnit 5 |
| **Cobertura de tests** | `PasswordGenerator` (5 tests), `PasswordValidator` (4 tests), rendimiento (2 tests) |
| **Lo que NO se testea** | `PasswordView`, `PasswordController`, `Main` (requieren UI/JavaFX) |
