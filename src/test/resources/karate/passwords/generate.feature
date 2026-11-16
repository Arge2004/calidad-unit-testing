Feature: Generación de contraseñas

  Background:
    * url baseUrl
    * path 'api', 'passwords', 'generate'

  Scenario: Sin opciones usa los valores por defecto y genera una contraseña fuerte
    Given request {}
    When method post
    Then status 200
    And match response == { password: '#string', strength: 'FUERTE', score: 5 }
    And match response.password == '#regex .{12}'

  Scenario Outline: Respeta la longitud solicitada (<length>)
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

  Scenario Outline: Solo usa la categoría seleccionada (<categoria>)
    Given request { length: 20, upper: <upper>, lower: <lower>, digits: <digits>, special: <special> }
    When method post
    Then status 200
    And match response.password == '#regex <patron>'
    And match response.strength == 'DEBIL'
    And match response.score == 2

    Examples:
      | categoria  | upper | lower | digits | special | patron        |
      | mayúsculas | true  | false | false  | false   | [A-Z]{20}     |
      | minúsculas | false | true  | false  | false   | [a-z]{20}     |
      | números    | false | false | true   | false   | [0-9]{20}     |
      | especiales | false | false | false  | true    | [^A-Za-z0-9]{20} |

  Scenario: Una contraseña corta es débil aunque incluya todas las categorías
    Given request { length: 6 }
    When method post
    Then status 200
    And match response.strength == 'DEBIL'
    And match response.score == 4

  Scenario Outline: Rechaza una longitud inválida (<length>)
    Given request { length: <length> }
    When method post
    Then status 400
    And match response == { error: '<mensaje>' }

    Examples:
      | length | mensaje                                                       |
      | 0      | La longitud de la contraseña debe ser mayor que cero.         |
      | -5     | La longitud de la contraseña debe ser mayor que cero.         |
      | 129    | La longitud de la contraseña no puede superar 128 caracteres. |

  Scenario: Rechaza la petición si no se selecciona ninguna categoría
    Given request { upper: false, lower: false, digits: false, special: false }
    When method post
    Then status 400
    And match response == { error: 'Se debe seleccionar al menos una categoría de caracteres.' }

  Scenario: Rechaza un cuerpo con tipos de dato incorrectos
    Given request { length: 'doce' }
    When method post
    Then status 400
    And match response == { error: 'El cuerpo de la petición no es un JSON válido.' }
