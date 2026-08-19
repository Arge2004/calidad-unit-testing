# agent.md

## Build & Test

```bash
# Run all tests (requires system Gradle — no wrapper present)
gradle test

# Run a single test class
gradle test --tests "com.passwordmanager.model.PasswordValidatorTest"

# Run a single test method
gradle test --tests "com.passwordmanager.model.PasswordGeneratorTest.testGenerate_CorrectLength"

# Run the app (requires JavaFX + display server)
gradle run

# Build without tests
gradle build -x test
```

Gradle 9.2.0, Java 26, JUnit Jupiter 5.10.0. **No `gradlew` wrapper** — uses system-installed `gradle`.

## Architecture

MVC pattern under `src/main/java/com/passwordmanager/`:

| File | Role |
|---|---|
| `Main.java` | JavaFX `Application` entrypoint. Creates models, view, controller and wires them. |
| `model/PasswordGenerator.java` | Generates passwords. Uses `SecureRandom`. Guarantees at least one char from each selected category. Shuffles output to avoid predictable order. |
| `model/PasswordValidator.java` | Scores passwords 0-5. Returns `ValidationResult` containing `Strength` enum (`DEBIL`/`MEDIA`/`FUERTE`), feedback list, and numeric score. |
| `controller/PasswordController.java` | Binds view events to model logic via lambda handlers. Measures validation time in nanoseconds. Auto-validates after generation. |
| `view/PasswordView.java` | JavaFX UI. Inline CSS via data URI (no external stylesheets). All styling embedded in `initUI()`. |

## Validation Rules (PasswordValidator)

5 independent rules, each adding 1 point to score (0-5):

| # | Rule | Regex / Condition |
|---|---|---|
| 1 | Length ≥ 8 | `password.length() >= 8` |
| 2 | Has uppercase | `.*[A-Z].*` |
| 3 | Has lowercase | `.*[a-z].*` |
| 4 | Has digit | `.*[0-9].*` |
| 5 | Has special char | `.*[!@#$%^&*()\\-_=+\[\]{}|;:,.<>?].*` |

**Strength mapping:** `length < 8` → DEBIL always; `score ≤ 2` → DEBIL; `score ≤ 4` → MEDIA; else FUERTE.

## Password Generation (PasswordGenerator)

- `SecureRandom` for cryptographic safety
- Guarantees at least one character from each enabled category (unless length < number of categories, then truncates guarantee list)
- Fills remaining length from combined pool, then `Collections.shuffle()` to avoid positional patterns
- Throws `IllegalArgumentException` if: length ≤ 0, or no category selected

## Testing

**Location:** `src/test/java/com/passwordmanager/`

| File | Tests | Focus |
|---|---|---|
| `model/PasswordValidatorTest.java` | 4 tests | Empty, short, medium, strong passwords |
| `model/PasswordGeneratorTest.java` | 5 tests | Correct length, single-category, invalid inputs, all-types guarantee |
| `performance/PasswordPerformanceTest.java` | 2 tests | Single gen <5ms, 10k bulk gen+val <500ms |

**Conventions:**
- `@DisplayName` annotations in Spanish
- Method naming: `test_<Method>_<Scenario>` (e.g., `testValidate_EmptyPassword`)
- AAA pattern with Spanish comments: `// Arrange (Organizar)`, `// Act (Actuar)`, `// Assert (Verificar)`
- Performance tests use hardcoded time thresholds — may be flaky on slow machines

**To add a new test:**
1. Place in appropriate package (`model/` for unit, `performance/` for benchmarks)
2. Follow `test_<Method>_<Scenario>` naming
3. Use `@DisplayName` in Spanish
4. Use AAA pattern with Spanish comments

## Dependencies

```groovy
// build.gradle
plugins {
    id 'application'
    id 'org.openjfx.javafxplugin' version '0.1.0'
}

dependencies {
    testImplementation 'org.junit.jupiter:junit-jupiter:5.10.0'
    testRuntimeOnly 'org.junit.platform:junit-platform-launcher'
}

javafx {
    version = "17"
    modules = ['javafx.controls', 'javafx.graphics', 'javafx.base']
}

application {
    mainClass = 'com.passwordmanager.Main'
}
```

## Gotchas

- **No Gradle wrapper** — collaborators must install Gradle 9.x separately
- **JavaFX requires a display server** — `gradle run` fails on headless environments
- **CSS is embedded as data URI** in `PasswordView.java:180-249` — not in external files
- **Test output goes to stdout** — `showStandardStreams = true` in build config
- **Validation scoring edge case:** a password with length < 8 is always DEBIL regardless of other rule matches (score is computed but strength is overridden)
