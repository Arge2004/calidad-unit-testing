Feature: Validación de contraseñas

  Background:
    * url baseUrl
    * path 'api', 'passwords', 'validate'

  Scenario: Una contraseña que cumple las cinco reglas es fuerte
    Given request { password: 'Abcdef1!' }
    When method post
    Then status 200
    And match response == { strength: 'FUERTE', score: 5, feedback: [] }

  Scenario Outline: Clasifica la fortaleza según las reglas cumplidas (<password>)
    Given request { password: '<password>' }
    When method post
    Then status 200
    And match response.strength == '<strength>'
    And match response.score == <score>
    And match response.feedback == '#[<faltantes>]'

    Examples:
      | password    | strength | score | faltantes |
      | abcdefgh    | DEBIL    | 2     | 3         |
      | Abcdefgh    | MEDIA    | 3     | 2         |
      | Abcdefg1    | MEDIA    | 4     | 1         |
      | Ab1!        | DEBIL    | 4     | 1         |
      | 12345678    | DEBIL    | 2     | 3         |

  Scenario: Explica qué regla falta
    Given request { password: 'abcdefg1!' }
    When method post
    Then status 200
    And match response.feedback == ['Debe contener al menos una letra mayúscula.']

  Scenario Outline: Una contraseña vacía es débil con puntaje cero (<caso>)
    Given request <cuerpo>
    When method post
    Then status 200
    And match response == { strength: 'DEBIL', score: 0, feedback: ['La contraseña no puede estar vacía.'] }

    Examples:
      | caso          | cuerpo             |
      | cadena vacía  | { password: '' }   |
      | solo espacios | { password: '  ' } |
      | sin campo     | {}                 |

  Scenario: Rechaza una petición sin cuerpo
    When method post
    Then status 400
    And match response == { error: 'El cuerpo de la petición no es un JSON válido.' }

  Scenario: No admite GET, para que la contraseña nunca viaje en la URL
    When method get
    Then status 405
