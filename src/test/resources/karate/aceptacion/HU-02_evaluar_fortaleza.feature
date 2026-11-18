@HU-02
Feature: HU-02 Evaluar la fortaleza de una contraseña

  Como usuario que ya tiene una contraseña
  Quiero saber qué tan fuerte es y qué le falta
  Para poder mejorarla antes de usarla

  Criterios de aceptación: ver docs/historias-de-usuario.md (cada escenario es un criterio).

  Background:
    * url baseUrl
    * path 'api', 'passwords', 'validate'

  @CA-02.1
  Scenario: CA-02.1 Una contraseña que cumple las cinco reglas es FUERTE
    Given request { password: 'Abcdef1!' }
    When method post
    Then status 200
    And match response == { strength: 'FUERTE', score: 5, feedback: [] }

  @CA-02.2
  Scenario Outline: CA-02.2 La fortaleza depende de las reglas cumplidas (<password>)
    Given request { password: '<password>' }
    When method post
    Then status 200
    And match response.strength == '<strength>'
    And match response.score == <score>
    And match response.feedback == '#[<faltantes>]'

    Examples:
      | password | strength | score | faltantes |
      | abcdefgh | DEBIL    | 2     | 3         |
      | 12345678 | DEBIL    | 2     | 3         |
      | Abcdefgh | MEDIA    | 3     | 2         |
      | Abcdefg1 | MEDIA    | 4     | 1         |

  @CA-02.3
  Scenario: CA-02.3 Una contraseña de menos de 8 caracteres siempre es DEBIL
    Given request { password: 'Ab1!' }
    When method post
    Then status 200
    And match response.strength == 'DEBIL'
    And match response.score == 4
    And match response.feedback == ['Debe tener al menos 8 caracteres.']

  @CA-02.4
  Scenario: CA-02.4 Se explica qué regla falta cumplir
    Given request { password: 'abcdefg1!' }
    When method post
    Then status 200
    And match response.feedback == ['Debe contener al menos una letra mayúscula.']

  @CA-02.5
  Scenario Outline: CA-02.5 Una contraseña vacía es DEBIL con puntaje cero (<caso>)
    Given request <cuerpo>
    When method post
    Then status 200
    And match response == { strength: 'DEBIL', score: 0, feedback: ['La contraseña no puede estar vacía.'] }

    Examples:
      | caso          | cuerpo             |
      | cadena vacía  | { password: '' }   |
      | solo espacios | { password: '  ' } |
      | sin campo     | {}                 |

  @CA-02.6
  Scenario: CA-02.6 Se rechaza una petición sin cuerpo
    When method post
    Then status 400
    And match response == { error: 'El cuerpo de la petición no es un JSON válido.' }

  @CA-02.7
  Scenario: CA-02.7 No se admite GET, para que la contraseña nunca viaje en la URL
    When method get
    Then status 405
