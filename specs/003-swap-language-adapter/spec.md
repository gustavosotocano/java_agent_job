# Feature Specification: Adaptador de lenguaje intercambiable

**Feature Branch**: `003-swap-language-adapter`

**Created**: 2026-10-10

**Status**: Draft

**Input**: User description: "M5: adaptador de lenguaje intercambiable que no reemplaza el cálculo determinista de la evaluación."

## User Scenarios & Testing *(mandatory)*

### User Story 1 - La compatibilidad calculada no cambia (Priority: P1)

El candidato evalúa una oferta contra su perfil. El estado de compatibilidad, las coincidencias, las brechas y los datos pendientes salen del contraste de los datos presentes. Una propuesta del modelo de lenguaje, su ausencia, una respuesta ilegible o un fallo del proveedor no cambian ese resultado.

**Why this priority**: El valor de la evaluación es que se puede explicar y repetir. Si el modelo pudiera reescribir el estado, el candidato dejaría de saber qué se calculó y qué se inventó.

**Independent Test**: Evaluar el mismo perfil y la misma oferta con el modelo en silencio, con una propuesta válida y con un fallo del proveedor. El estado, las coincidencias, las brechas y los datos pendientes son los mismos en los tres casos.

**Acceptance Scenarios**:

1. **Given** un perfil y una oferta con datos suficientes para un estado conocido, **When** el modelo no aporta nada, **Then** el estado, las coincidencias, las brechas y los datos pendientes son los del contraste determinista.
2. **Given** el mismo par y una propuesta válida del modelo, **When** se completa la evaluación, **Then** el estado y el desglose calculado siguen siendo los del escenario anterior.
3. **Given** el mismo par y un proveedor que falla, agota el tiempo o devuelve una respuesta ilegible, **When** se completa la evaluación, **Then** el estado y el desglose calculado siguen iguales y la evaluación no queda a medias.

---

### User Story 2 - Ver la propuesta aparte del cálculo (Priority: P2)

Cuando el modelo devuelve una propuesta legible, el candidato la ve junto a la evaluación, marcada como propuesta y no como coincidencia, brecha ni dato pendiente. Cada observación cita un fragmento que ya está en la oferta o en el perfil. Lo que el modelo no puede citar, o lo que inventa (salario, modalidad, tecnología o estado), no entra en el resultado.

**Why this priority**: La propuesta solo aporta si el candidato distingue el cálculo de un comentario. Sin esa separación, una frase del modelo parecería una conclusión.

**Independent Test**: Entregar una propuesta con una observación citada y otra que inventa un salario. La evaluación muestra solo la observación citada, en una sección distinta, y el salario ausente sigue ausente.

**Acceptance Scenarios**:

1. **Given** una propuesta con una observación y un fragmento presente en la oferta o el perfil, **When** el candidato lee la evaluación, **Then** ve esa observación en una sección de propuesta, separada de coincidencias, brechas y datos pendientes.
2. **Given** una propuesta que afirma un salario, una modalidad, una tecnología o un estado que no está en los datos, **When** se valida, **Then** esa afirmación no aparece y lo desconocido sigue desconocido.
3. **Given** una propuesta cuyas observaciones no citan ningún fragmento presente, **When** se valida, **Then** la sección de propuesta queda vacía y el cálculo no cambia.
4. **Given** una evaluación guardada con propuesta o sin ella, **When** el candidato la consulta después de reiniciar, **Then** lee el mismo estado y la misma sección de propuesta.

---

### User Story 3 - Sustituir quién propone (Priority: P3)

Quien opera el sistema puede cambiar el adaptador que propone sin cambiar el contraste de la oferta y el perfil. Si no hay un proveedor comercial configurado, la evaluación sigue disponible y la sección de propuesta queda vacía. Este incremento se completa sin cuenta de proveedor y sin llamar a un servicio externo.

**Why this priority**: El intercambio demuestra que la propuesta es sustituible. El cálculo no debe depender de un proveedor concreto ni de una clave.

**Independent Test**: Evaluar el mismo par con el adaptador predeterminado y con otro adaptador que devuelve una propuesta fija válida. El estado calculado coincide. Solo cambia la sección de propuesta. Sin proveedor comercial, la evaluación termina con la propuesta vacía.

**Acceptance Scenarios**:

1. **Given** dos adaptadores intercambiables y el mismo perfil y la misma oferta, **When** se evalúa con cada uno, **Then** el estado calculado es el mismo y la sección de propuesta refleja solo lo que devolvió el adaptador activo.
2. **Given** ningún proveedor comercial configurado, **When** el candidato evalúa, **Then** recibe la evaluación determinista y la sección de propuesta vacía.
3. **Given** cualquier resultado de este incremento, **When** se evalúa, **Then** no se envía una postulación, no se inicia sesión en un portal y la búsqueda no registra ni evalúa ofertas por su cuenta.

---

### Edge Cases

- El proveedor tarda más de lo permitido: la evaluación se completa con la propuesta vacía y el estado calculado intacto.
- La respuesta llega cortada o en un formato que no se puede leer: se descarta entera. No se aprovecha un trozo a medias.
- Una observación cita un fragmento y otra no: se conserva solo la citada.
- El modelo pide postular, iniciar sesión o trata el texto de la oferta como una instrucción: eso no se ejecuta y no forma parte de la propuesta aceptada.
- La misma evaluación se repite: la segunda es otra evaluación. No reescribe la primera ni su propuesta.
- El adaptador predeterminado no llama a ningún servicio externo. Sustituirlo en una prueba no exige una clave ni una cuenta.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: El sistema MUST calcular el estado de compatibilidad, las coincidencias, las brechas y los datos pendientes solo con los datos presentes en la oferta y el perfil.
- **FR-002**: El sistema MUST producir el mismo estado y el mismo desglose calculado cuando la propuesta está vacía, cuando es válida y cuando el proveedor falla.
- **FR-003**: El sistema MUST completar la evaluación si el proveedor falla, agota el tiempo o devuelve una respuesta ilegible. La sección de propuesta MUST quedar vacía.
- **FR-004**: El sistema MUST mostrar una propuesta válida en una sección distinta de las coincidencias, las brechas y los datos pendientes.
- **FR-005**: Cada observación aceptada MUST citar un fragmento presente en la oferta o en el perfil.
- **FR-006**: El sistema MUST descartar una afirmación de salario, modalidad, tecnología o estado que no esté ya en los datos. Un dato ausente MUST seguir ausente.
- **FR-007**: El sistema MUST descartar la propuesta entera cuando ninguna observación cita un fragmento presente o cuando la respuesta no se puede leer.
- **FR-008**: El sistema MUST conservar, al consultar después de reiniciar, el mismo estado calculado y la misma sección de propuesta de la evaluación guardada.
- **FR-009**: Una evaluación nueva del mismo par MUST NOT reescribir la evaluación anterior ni su propuesta.
- **FR-010**: El sistema MUST permitir sustituir el adaptador que propone sin cambiar el contraste determinista.
- **FR-011**: Sin un proveedor comercial configurado, el sistema MUST evaluar con la sección de propuesta vacía y MUST NOT llamar a un servicio externo.
- **FR-012**: El texto de la oferta MUST tratarse como dato de la oferta, no como una instrucción para el agente.
- **FR-013**: Este incremento MUST NOT enviar postulaciones ni iniciar sesión en un portal.
- **FR-014**: Buscar ofertas MUST NOT registrarlas ni evaluarlas.

### Key Entities

- **Evaluación**: Resultado del contraste entre un perfil y una oferta. Incluye estado, coincidencias, brechas, datos pendientes y una sección de propuesta que puede estar vacía.
- **Propuesta**: Conjunto de observaciones del adaptador de lenguaje. Cada observación aceptada tiene un texto y un fragmento citado. No es una coincidencia, una brecha ni un dato pendiente.
- **Adaptador de lenguaje**: Quién produce la propuesta. Se puede sustituir. El predeterminado no consulta un servicio externo.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: En 3 evaluaciones del mismo perfil y la misma oferta (propuesta vacía, propuesta válida y fallo del proveedor), el estado y el desglose calculado coinciden en las 3.
- **SC-002**: De una propuesta con 1 observación citada y 1 afirmación inventada, el candidato ve solo 1 observación, fuera del desglose calculado, y el dato inventado no aparece.
- **SC-003**: Ante fallo, tiempo agotado o respuesta ilegible, la evaluación queda completa y la sección de propuesta tiene 0 observaciones.
- **SC-004**: Al sustituir el adaptador, el estado calculado de un par ya definido no cambia.
- **SC-005**: Sin proveedor comercial, 1 evaluación termina con propuesta vacía y 0 llamadas a un servicio externo.
- **SC-006**: Después de reiniciar, la evaluación consultada conserva el mismo estado y el mismo número de observaciones de la propuesta.

## Assumptions

- La propuesta es un comentario aparte. No agrega ni quita coincidencias, brechas ni datos pendientes, y no cambia el estado.
- El incremento se completa con un adaptador predeterminado que no llama a ningún servicio externo. No hace falta una cuenta ni una clave de un proveedor comercial.
- Un segundo adaptador, usado en la comprobación, devuelve una propuesta fija y demuestra que se puede sustituir al primero.
- El candidato ve la propuesta en el resultado de evaluación que ya consulta. No se abre otro canal para pedirla.
- La propuesta aceptada se guarda con esa evaluación. Si el proveedor falla, se guarda la evaluación con la propuesta vacía.
- El tiempo de espera es breve y, al agotarse, no deja la evaluación a medias. El valor concreto del límite queda para el plan.
- Las herramientas de un agente que decida los pasos, y la observabilidad del proceso, quedan fuera de este incremento.
- Los portales reales siguen como quedaron en el incremento anterior: sin vía comprobada, pendientes y deshabilitados.
