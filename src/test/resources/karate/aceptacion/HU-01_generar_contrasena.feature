@HU-01
Feature: HU-01 Generar una contraseña segura

  Como usuario que crea cuentas en distintos servicios
  Quiero generar una contraseña aleatoria eligiendo su longitud y los tipos de caracteres
  Para no tener que inventarla y usar una que sea difícil de adivinar

  Criterios de aceptación: ver docs/historias-de-usuario.md (cada escenario es un criterio).

  Background:
    * url baseUrl
    * path 'api', 'passwords', 'generate'

  @CA-01.1
  Scenario: CA-01.1 Sin opciones se genera una contraseña fuerte de 12 caracteres
    Given request {}
    When method post
    Then status 200
    And match response == { password: '#string', strength: 'FUERTE', score: 5 }
    And match response.password == '#regex .{12}'

  @CA-01.2
  Scenario Outline: CA-01.2 La contraseña tiene exactamente la longitud solicitada (<length>)
    Given request { length: <length> }
    When method post
    Then status 200
    And match response.password == '#regex .{<length>}'

    Examples:
      | length |
      | 1      |
      | 8      |
      | 32     |
      | 128    |

  @CA-01.3
  Scenario Outline: CA-01.3 Solo se usan las categorías seleccionadas (<categoria>)
    Given request { length: 20, upper: <upper>, lower: <lower>, digits: <digits>, special: <special> }
    When method post
    Then status 200
    And match response.password == '#regex <patron>'

    Examples:
      | categoria  | upper | lower | digits | special | patron           |
      | mayúsculas | true  | false | false  | false   | [A-Z]{20}        |
      | minúsculas | false | true  | false  | false   | [a-z]{20}        |
      | números    | false | false | true   | false   | [0-9]{20}        |
      | especiales | false | false | false  | true    | [^A-Za-z0-9]{20} |

  @CA-01.4
  Scenario: CA-01.4 Incluye al menos un carácter de cada categoría seleccionada
    Given request { length: 4, upper: true, lower: true, digits: true, special: true }
    When method post
    Then status 200
    And match response.password == '#regex (?=.*[A-Z])(?=.*[a-z])(?=.*[0-9])(?=.*[^A-Za-z0-9]).{4}'

  @CA-01.5
  Scenario: CA-01.5 La respuesta informa la fortaleza de la contraseña generada
    Given request { length: 6 }
    When method post
    Then status 200
    And match response.strength == 'DEBIL'
    And match response.score == 4

  @CA-01.5
  Scenario: CA-01.5 Una sola categoría produce una contraseña débil aunque sea larga
    Given request { length: 20, upper: false, lower: true, digits: false, special: false }
    When method post
    Then status 200
    And match response.strength == 'DEBIL'
    And match response.score == 2

  @CA-01.6
  Scenario Outline: CA-01.6 Se rechaza una longitud fuera del rango 1-128 (<length>)
    Given request { length: <length> }
    When method post
    Then status 400
    And match response == { error: '<mensaje>' }

    Examples:
      | length | mensaje                                                       |
      | 0      | La longitud de la contraseña debe ser mayor que cero.         |
      | -5     | La longitud de la contraseña debe ser mayor que cero.         |
      | 129    | La longitud de la contraseña no puede superar 128 caracteres. |

  @CA-01.7
  Scenario: CA-01.7 Se rechaza la petición si no se selecciona ninguna categoría
    Given request { upper: false, lower: false, digits: false, special: false }
    When method post
    Then status 400
    And match response == { error: 'Se debe seleccionar al menos una categoría de caracteres.' }

  @CA-01.8
  Scenario: CA-01.8 Se rechaza un cuerpo con tipos de dato incorrectos
    Given request { length: 'doce' }
    When method post
    Then status 400
    And match response == { error: 'El cuerpo de la petición no es un JSON válido.' }
