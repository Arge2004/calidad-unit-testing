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

# Run the REST API (Spring Boot, port 8080; override with --args=--server.port=8081)
gradle bootRun

# Build without tests
gradle build -x test

# Static analysis on SonarCloud (project Arge2004_calidad-unit-testing, org arge2004)
SONAR_TOKEN=<token> gradle test sonar

# Same analysis against a local SonarQube
SONAR_HOST_URL=http://localhost:9000 SONAR_TOKEN=<token> gradle test sonar
```

Gradle 9.x, Java 21+, Spring Boot 4.1, JUnit Jupiter 6 (version managed by the Spring Boot BOM), Karate 2.1. **No `gradlew` wrapper** — uses system-installed `gradle`.

## Architecture

MVC pattern under `src/main/java/com/passwordmanager/`:

| File | Role |
|---|---|
| `Main.java` | JavaFX `Application` entrypoint. Creates models, view, controller and wires them. |
| `model/PasswordGenerator.java` | Generates passwords. Uses `SecureRandom`. Guarantees at least one char from each selected category. Shuffles output to avoid predictable order. |
| `model/PasswordValidator.java` | Scores passwords 0-5. Returns `ValidationResult` containing `Strength` enum (`DEBIL`/`MEDIA`/`FUERTE`), feedback list, and numeric score. |
| `controller/PasswordController.java` | Binds view events to model logic via lambda handlers. Measures validation time in nanoseconds. Auto-validates after generation. |
| `api/ApiApplication.java` | Spring Boot entrypoint for the REST API. Registers the two models as beans. Independent from the JavaFX app. |
| `api/PasswordApiController.java` | `POST /api/passwords/generate` and `POST /api/passwords/validate`. Caps length at 128. |
| `api/ApiExceptionHandler.java` | Maps `IllegalArgumentException` and unreadable bodies to `400 {"error": "..."}`. |
| `api/dto/*` | Request/response records. `GenerateRequest` defaults omitted fields to length 12 and all categories. |
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
| `api/PasswordAcceptanceTest.java` | 1 test (29 Karate scenario runs) | Acceptance tests: boots the API on a random port and runs every `.feature` under `src/test/resources/karate/aceptacion/` over HTTP. One feature per user story (`@HU-xx`), one scenario per acceptance criterion (`@CA-xx.y`); stories and traceability matrix in `docs/historias-de-usuario.md` |

Karate HTML report: `build/karate-reports/karate-summary.html`. JaCoCo report: `build/reports/jacoco/test/html/index.html`.

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

See `build.gradle`. Spring and JUnit versions come from the Spring Boot BOM; only the Spring Boot, Sonar and JavaFX plugins and Karate carry explicit versions.

## Coverage scope (deliberate exclusion)

Coverage is measured only over the **API and business logic** (`model/` and `api/`). The JavaFX desktop layer — `view/`, `controller/` and `Main.java` — is **excluded on purpose** from both the JaCoCo report and Sonar's coverage metric: it needs a graphical environment, and the current goal is to test the API and business rules. This is a scope decision, not an oversight; do not "fix" it by adding those classes back without agreeing a UI testing approach (e.g. TestFX).

The exclusion only affects *coverage*. Sonar's static analysis (bugs, smells, vulnerabilities) still scans every file. The list lives in `build.gradle` (`coverageExcludedClasses` and `sonar.coverage.exclusions`) — keep both in sync.

## Gotchas

- **No Gradle wrapper** — collaborators must install Gradle 9.x separately
- **JavaFX requires a display server** — `gradle run` fails on headless environments
- **CSS is embedded as data URI** in `PasswordView.java:180-249` — not in external files
- **`ignoreFailures = true` on the `test` task** — the build is green even when tests fail (so JaCoCo/Sonar still get a report); read the test report, not the build status
- **Two entrypoints** — `gradle run` starts JavaFX (`Main`), `gradle bootRun` starts the API (`ApiApplication`); `bootRun`/`bootJar` pin their main class explicitly because the `application` plugin would otherwise hand them the JavaFX one
- **Validation scoring edge case:** a password with length < 8 is always DEBIL regardless of other rule matches (score is computed but strength is overridden)
