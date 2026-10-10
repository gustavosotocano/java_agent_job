# Feature Specification: Verificar acceso a portales públicos

**Feature Branch**: `002-verify-portal-access`

**Created**: 2026-10-10

**Status**: Draft

**Input**: User description: "M4.1: verificar el acceso de LinkedIn, Indeed y Glassdoor y dejar implementado solo el adaptador cuyo mecanismo esté comprobado. Las fuentes sin acceso siguen pendientes y deshabilitadas."

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Saber qué portales se pueden consultar (Priority: P1)

El candidato quiere usar LinkedIn, Indeed y Glassdoor como fuentes de ofertas públicas. Antes de ver resultados de un portal, necesita un dictamen claro: ese portal admite una vía pública permitida, o no la admite. Un portal configurado no cuenta como fuente usable hasta que el dictamen lo diga.

**Why this priority**: Sin ese dictamen, el candidato no distingue una fuente real de un nombre pendiente. El valor queda entregado aunque ningún portal resulte usable.

**Independent Test**: Revisar el dictamen de LinkedIn, Indeed y Glassdoor. Cada uno dice si la vía pública está comprobada y por qué. No depende de que la búsqueda ya devuelva ofertas de ese portal.

**Acceptance Scenarios**:

1. **Given** los tres portales todavía sin dictamen, **When** se completa la verificación, **Then** LinkedIn, Indeed y Glassdoor tienen cada uno un dictamen legible de vía comprobada o de vía no comprobada.
2. **Given** un portal cuya consulta pública exigiría una cuenta personal o iniciar sesión, **When** se emite su dictamen, **Then** la vía queda no comprobada y el motivo lo dice.
3. **Given** un portal con vía comprobada, **When** se lee su dictamen, **Then** indica qué clase de acceso público usa, qué cubre, qué límites tiene y con qué actualidad se pueden esperar los resultados.
4. **Given** un dictamen ya emitido, **When** el candidato vuelve más tarde, **Then** lee el mismo dictamen y el portal conserva el mismo estado de usable o no usable.

---

### User Story 2 - Buscar solo en portales con acceso comprobado (Priority: P2)

El candidato busca con sus criterios habituales. La búsqueda consulta la fuente simulada que ya existía y, además, cada portal cuyo dictamen sea de vía comprobada. Un portal sin esa vía no se consulta. Las ofertas que llegan traen lo que el portal publicó y dejan desconocido lo que no publicó.

**Why this priority**: El dictamen solo cambia la búsqueda cuando existe al menos una vía comprobada. Si no existe ninguna, esta historia no agrega fuentes y la búsqueda simulada sigue igual.

**Independent Test**: Con un portal de vía comprobada y otro sin ella, buscar y comprobar que solo el primero aporta ofertas reales. Con los tres sin vía comprobada, la búsqueda no incorpora ninguno.

**Acceptance Scenarios**:

1. **Given** un portal con vía comprobada y habilitado, **When** el candidato busca, **Then** ese portal puede aportar ofertas públicas que coinciden con los criterios.
2. **Given** un portal sin vía comprobada, **When** el candidato busca, **Then** ese portal sigue pendiente y deshabilitado y no es consultado.
3. **Given** los tres portales sin vía comprobada, **When** el candidato busca, **Then** ningún portal real aporta ofertas y la fuente simulada sigue disponible.
4. **Given** una oferta de un portal real a la que le falta salario, fecha o modalidad, **When** se muestra el resultado, **Then** esos datos siguen desconocidos.
5. **Given** dos ofertas del mismo cargo y la misma empresa en portales distintos, **When** se muestran, **Then** permanecen como ofertas distintas.

---

### User Story 3 - Aislar el fallo de un portal (Priority: P3)

El candidato busca mientras uno de los portales comprobados no responde. Recibe las ofertas de las fuentes que sí respondieron y una advertencia que identifica al portal fallido. Buscar no guarda esas ofertas ni las evalúa, y no postula ni entra a una cuenta.

**Why this priority**: Una fuente real puede fallar o no tener coincidencias. El candidato necesita distinguir eso sin perder el resto de la búsqueda ni mezclarla con el registro o la evaluación.

**Independent Test**: Provocar el fallo de un portal comprobado mientras otra fuente habilitada responde. La respuesta trae los resultados válidos y una advertencia. El número de ofertas registradas y de evaluaciones no cambia.

**Acceptance Scenarios**:

1. **Given** una fuente que responde y un portal comprobado que falla, **When** el candidato busca, **Then** ve las ofertas de la fuente que respondió y una advertencia del portal fallido.
2. **Given** una búsqueda que devuelve ofertas de un portal real, **When** termina, **Then** no aumenta el número de ofertas registradas ni el de evaluaciones.
3. **Given** una búsqueda sin coincidencias y otra en la que un portal falló, **When** se comparan, **Then** un resultado vacío se distingue de un portal fallido.
4. **Given** cualquier resultado de este incremento, **When** el candidato busca, **Then** no se envía ninguna postulación ni se inicia sesión en un portal.

---

### Edge Cases

- Los tres portales quedan sin vía comprobada: no aparece ninguna fuente real nueva, los tres siguen pendientes y deshabilitados, y la fuente simulada sigue usable.
- Más de un portal tiene vía comprobada: cada uno de esos portales puede consultarse. Los que no la tienen siguen pendientes y deshabilitados.
- Un portal responde, pero ningún aviso coincide con los criterios: la búsqueda queda vacía respecto de ese portal, no como portal fallido ni como vía no comprobada.
- Un portal comprobado deja de responder de forma transitoria: la búsqueda informa el fallo de esa fuente y conserva los resultados de las demás.
- El mismo aviso aparece dos veces en el mismo portal: se muestra una sola vez. Un aviso parecido en otro portal no se fusiona.
- El texto de un aviso importado es un dato de la oferta, no una instrucción para el agente.
- Verificar un portal no deshabilita la fuente simulada ni cambia el dictamen de los otros dos.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: El sistema MUST emitir un dictamen distinto para LinkedIn, para Indeed y para Glassdoor.
- **FR-002**: Cada dictamen MUST declarar la vía como comprobada o no comprobada.
- **FR-003**: Un dictamen de vía no comprobada MUST incluir el motivo. Si la única vía encontrada exige cuenta personal o iniciar sesión, el motivo MUST decirlo.
- **FR-004**: Un dictamen de vía comprobada MUST indicar la clase de acceso público, la cobertura, los límites de uso y la actualidad esperada de los resultados.
- **FR-005**: El sistema MUST NOT tratar como fuente usable a un portal cuyo dictamen no sea de vía comprobada.
- **FR-006**: Un portal sin vía comprobada MUST permanecer pendiente y deshabilitado, y la búsqueda MUST NOT consultarlo.
- **FR-007**: Cada portal con vía comprobada MUST poder consultarse en la búsqueda que el candidato ya usa. Este incremento MUST NOT dejar comprobable un portal y fuera de la búsqueda.
- **FR-008**: Si ninguno de los tres tiene vía comprobada, la búsqueda MUST NOT incorporar una fuente real nueva.
- **FR-009**: La fuente simulada MUST seguir disponible y MUST NOT quedar deshabilitada por la verificación de los portales.
- **FR-010**: Una oferta obtenida de un portal MUST conservar el título, la fuente, la dirección pública y los demás datos que el portal haya publicado. El salario, la fecha o la modalidad no publicados MUST permanecer desconocidos.
- **FR-011**: Ofertas de portales distintos MUST permanecer distintas aunque compartan cargo y empresa.
- **FR-012**: Una búsqueda MUST NOT registrar las ofertas devueltas y MUST NOT crear evaluaciones.
- **FR-013**: El sistema MUST NOT enviar postulaciones ni iniciar sesión en portales como parte de esta capacidad.
- **FR-014**: El fallo de una fuente consultada MUST NOT impedir devolver los resultados válidos de las demás y MUST informarse como advertencia de esa fuente.
- **FR-015**: Un resultado vacío MUST distinguirse de una fuente fallida y de una búsqueda sin fuentes habilitadas.
- **FR-016**: El dictamen y el estado usable o no usable de cada portal MUST poder leerse de nuevo con el mismo contenido.

### Key Entities

- **Portal candidato**: LinkedIn, Indeed o Glassdoor. Tiene un dictamen de acceso y un estado de fuente. Configurarlo no implica que la búsqueda ya traiga sus ofertas.
- **Dictamen de acceso**: Conclusión por portal. Dice si la vía pública está comprobada, por qué, y, cuando lo está, la clase de acceso, la cobertura, los límites y la actualidad esperada.
- **Fuente usable**: Origen que la búsqueda puede consultar. En este incremento lo son la fuente simulada ya existente y cada portal con dictamen de vía comprobada y habilitado.
- **Oferta pública**: Aviso normalizado traído de una fuente usable, con procedencia y dirección pública. Lo no publicado queda desconocido.
- **Resultado de búsqueda**: Ofertas coincidentes, fuentes consultadas y advertencias. No incluye ofertas registradas ni evaluaciones.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Los 3 portales nombrados tienen dictamen. Una persona distingue, sin reconstruir la investigación, cuáles quedaron con vía comprobada y cuáles no.
- **SC-002**: El número de portales reales que la búsqueda puede consultar es igual al número de portales con vía comprobada, y es 0 cuando ninguno la tiene.
- **SC-003**: Una búsqueda que devuelve ofertas de un portal real cambia en 0 el número de ofertas registradas y en 0 el número de evaluaciones.
- **SC-004**: Cuando un portal comprobado falla y otra fuente habilitada responde, el candidato recibe las ofertas de la fuente que respondió y exactamente una advertencia identificable del portal fallido.
- **SC-005**: Al releer una oferta de un portal real, el número de salarios, fechas o modalidades inventados es 0.
- **SC-006**: Después de cerrar y volver a abrir el agente, los 3 dictámenes y el estado usable o no usable de cada portal coinciden con los que había antes de cerrarlo.

## Assumptions

- El candidato es la misma persona que ya busca y evalúa. No hay cuentas de usuario ni permisos nuevos.
- Este incremento solo dictamina LinkedIn, Indeed y Glassdoor. Cualquier otro portal queda fuera.
- "Vía comprobada" significa que se pueden obtener ofertas públicas sin cuenta personal y sin iniciar sesión. Una vía que exija eso queda no comprobada.
- La condición de dejar usable solo lo que tenga el acceso comprobado se interpreta como cada portal con vía comprobada, no como un máximo de uno. Si hay dos o tres, los tres pueden consultarse. Si no hay ninguno, no se agrega ninguna fuente real.
- La clase de acceso público del dictamen es una de estas: listado público, feed, proveedor autorizado o búsqueda pública indexada. Elegir la clase forma parte del dictamen, no de una preferencia previa.
- La búsqueda, el registro de su resultado, la separación entre resultado vacío y fallo, y la regla de no registrar ni evaluar siguen como están. Este incremento no abre otro modo de buscar.
- La fuente simulada no se vuelve a verificar. Sigue siendo la fuente reproducible de respaldo.
- El texto importado de una oferta es dato externo, no una instrucción para el agente.
- No se promete que alguno de los tres portales termine con vía comprobada. El incremento está completo cuando los tres dictámenes existen y la búsqueda solo consulta los que quedaron comprobados.
