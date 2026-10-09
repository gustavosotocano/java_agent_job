# Feature Specification: Persistencia de oportunidades

**Feature Branch**: `001-persist-opportunities`

**Created**: 2026-10-09

**Status**: Draft

**Input**: User description: "Hito M4. Las ofertas registradas, las evaluaciones completadas, las fuentes configuradas y el registro de las búsquedas deben sobrevivir a un reinicio. Reimportar la misma oferta no debe duplicarla. Buscar sigue sin registrar ni evaluar."

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Recuperar una evaluación después de reiniciar (Priority: P1)

El candidato evalúa una oferta contra su perfil, cierra el agente y vuelve más tarde. La evaluación sigue disponible, con el mismo estado de compatibilidad, las mismas coincidencias, las mismas brechas, los mismos datos pendientes y la evidencia que los sustenta.

**Why this priority**: Sin esta continuidad, cada reinicio borra el trabajo de comparación ya hecho y el candidato no puede retomar una decisión.

**Independent Test**: Completar una evaluación, detener y volver a iniciar el agente, y recuperar esa evaluación. El valor queda entregado aunque todavía no exista deduplicación de ofertas ni historial de búsquedas.

**Acceptance Scenarios**:

1. **Given** una evaluación completada con estado, coincidencias, brechas y datos pendientes, **When** el agente se detiene y vuelve a iniciar, **Then** esa evaluación se recupera con el mismo contenido y la misma evidencia.
2. **Given** dos evaluaciones distintas de la misma oferta y el mismo perfil, **When** el agente reinicia, **Then** ambas siguen disponibles y ninguna reemplaza a la otra.
3. **Given** una evaluación cuya oferta no publicaba salario, **When** se recupera la evaluación, **Then** el salario sigue figurando como desconocido y no como cero ni como un importe inventado.

---

### User Story 2 - Reimportar una oferta sin duplicarla (Priority: P2)

El candidato registra una oferta publicada y, más adelante, vuelve a traer la misma publicación desde la misma fuente. El agente conserva una sola oferta, actualiza la captura más reciente y no pierde las procedencias anteriores.

**Why this priority**: Las fuentes se consultan más de una vez. Sin esta regla, el candidato vería copias del mismo puesto y no sabría cuál es la vigente.

**Independent Test**: Registrar dos veces la misma publicación de la misma fuente y comprobar que queda una sola oferta, con ambas capturas en su procedencia. No depende del historial de búsquedas.

**Acceptance Scenarios**:

1. **Given** una oferta ya registrada, **When** se registra de nuevo la misma publicación de la misma fuente y la misma dirección pública, **Then** sigue existiendo una sola oferta.
2. **Given** esa reimportación, **When** se consulta la oferta, **Then** conserva cada procedencia y el momento de cada captura.
3. **Given** dos publicaciones con el mismo cargo y la misma empresa pero distinta fuente o distinta dirección pública, **When** se registran ambas, **Then** permanecen como ofertas distintas.
4. **Given** una oferta sin salario, fecha o modalidad publicados, **When** se guarda y se vuelve a leer, **Then** esos datos siguen ausentes.

---

### User Story 3 - Recordar una búsqueda sin guardar sus ofertas (Priority: P3)

El candidato busca con unos criterios. El agente recuerda qué se pidió, qué fuentes se consultaron, qué advertencias hubo y si alguna fuente falló. Las ofertas devueltas no quedan registradas y no se crea ninguna evaluación por el solo hecho de buscar.

**Why this priority**: Permite explicar un resultado vacío o un fallo parcial después de reiniciar, sin mezclar la búsqueda con el registro ni con la evaluación.

**Independent Test**: Ejecutar una búsqueda, reiniciar y leer el registro de esa búsqueda. El número de ofertas registradas y el de evaluaciones no cambia.

**Acceptance Scenarios**:

1. **Given** una búsqueda con criterios, fuentes consultadas y advertencias, **When** el agente reinicia, **Then** ese registro sigue disponible y distinguible de otras búsquedas.
2. **Given** una búsqueda que devuelve ofertas, **When** termina, **Then** no aumenta el número de ofertas registradas ni el de evaluaciones completadas.
3. **Given** una búsqueda sin resultados y otra en la que una fuente falló, **When** se leen ambos registros, **Then** se distingue un resultado vacío de una fuente fallida.
4. **Given** una fuente habilitada, deshabilitada, pendiente o disponible, **When** el agente reinicia, **Then** esa fuente conserva el mismo estado.

---

### Edge Cases

- Registrar la misma publicación dos veces de forma simultánea deja una sola oferta.
- Un cambio de título en la misma dirección pública de la misma fuente no crea otra oferta; la identidad sigue siendo la fuente más la dirección pública.
- Cargos parecidos en portales distintos no se fusionan, aunque coincidan empresa, título y ubicación.
- Una evaluación ya guardada sigue apuntando a la misma oferta después de reimportarla.
- Una búsqueda con todas las fuentes deshabilitadas queda registrada como búsqueda sin fuentes consultadas, no como un fallo de una fuente que sí se consultó.
- Detener el agente a mitad de un registro no deja una oferta a medias: o el registro completo queda guardado, o no queda ninguno nuevo.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: El sistema MUST conservar cada evaluación completada de forma que pueda recuperarse después de detener e iniciar de nuevo el agente.
- **FR-002**: Una evaluación recuperada MUST mostrar el mismo estado, las mismas coincidencias, las mismas brechas, los mismos datos pendientes y la evidencia usada para cada conclusión.
- **FR-003**: El sistema MUST conservar cada oferta registrada después de un reinicio, incluidos título, empresa, descripción, ubicación, modalidad cuando exista, salario cuando exista, dirección pública, fuente y fechas disponibles.
- **FR-004**: Registrar de nuevo una oferta de la misma fuente y la misma dirección pública MUST dejar una sola oferta.
- **FR-005**: Esa oferta única MUST conservar cada procedencia registrada y el momento de cada captura.
- **FR-006**: Ofertas con el mismo cargo y la misma empresa MUST permanecer distintas cuando provienen de otra fuente o de otra dirección pública.
- **FR-007**: El sistema MUST NOT completar salario, fecha o modalidad que no estuvieran publicados al capturar o al recuperar una oferta.
- **FR-008**: El sistema MUST NOT comparar importes de distinta moneda o distinto período al conservar o recuperar una evaluación.
- **FR-009**: Realizar una búsqueda MUST NOT registrar las ofertas devueltas y MUST NOT crear evaluaciones.
- **FR-010**: El sistema MUST poder guardar un registro de búsqueda con sus criterios, las fuentes consultadas, las advertencias y los fallos parciales.
- **FR-011**: Un registro de búsqueda MUST distinguir un resultado vacío de una fuente fallida.
- **FR-012**: Una nueva evaluación de una oferta y un perfil ya evaluados MUST guardarse como otra evaluación y MUST NOT borrar la anterior.
- **FR-013**: El estado configurado de cada fuente MUST conservarse después de un reinicio.
- **FR-014**: El sistema MUST NOT enviar postulaciones ni iniciar sesión en portales externos como parte de esta capacidad.
- **FR-015**: El perfil y los datos de la oferta necesarios para explicar una evaluación MUST conservarse con esa evaluación, de modo que el resultado siga siendo comprensible después de un reinicio.

### Key Entities *(include if feature involves data)*

- **Oferta laboral**: Publicación normalizada. Se identifica por la fuente y su dirección pública. Incluye título, empresa, descripción, ubicación, modalidad opcional, salario opcional, dirección original y fechas disponibles.
- **Procedencia**: Captura de una oferta en una fuente, con el momento en que se tomó. Una oferta puede acumular varias procedencias sin dejar de ser una sola.
- **Evaluación**: Resultado explicable de contrastar una oferta con un perfil. Incluye estado, coincidencias, brechas, datos pendientes, evidencia y momento de la evaluación. Es inmutable: una repetición crea otra evaluación.
- **Perfil profesional**: Conjunto de datos del candidato usado como evidencia de la evaluación. Se conserva lo necesario para volver a leer el resultado, no para postular.
- **Registro de búsqueda**: Criterios pedidos, fuentes consultadas, advertencias, fallos parciales y la distinción entre resultado vacío y búsqueda fallida. No contiene ofertas registradas ni evaluaciones.
- **Fuente configurada**: Origen de ofertas con un estado de habilitada, deshabilitada, pendiente o disponible. Configurar una fuente no implica que la búsqueda ya traiga publicaciones reales de ese portal.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Después de detener e iniciar el agente, el 100 % de las evaluaciones completadas antes del reinicio se recuperan con el mismo estado y la misma evidencia.
- **SC-002**: Importar 10 veces la misma publicación desde la misma fuente deja exactamente 1 oferta y conserva las 10 capturas en su procedencia.
- **SC-003**: Una búsqueda que devuelve ofertas cambia en 0 el número de ofertas registradas y en 0 el número de evaluaciones.
- **SC-004**: En una lectura, una persona distingue un registro de búsqueda vacía de un registro con una fuente fallida, sin reconstruir la petición original.
- **SC-005**: Al releer una oferta guardada, el número de salarios, fechas o modalidades inventados es 0.
- **SC-006**: Dos ofertas de portales distintos, aunque compartan empresa y título, siguen contando como 2 ofertas después de guardarlas y reiniciar.

## Assumptions

- El incremento cubre conservar lo ya producido por buscar, registrar y evaluar. No cambia las reglas de compatibilidad ni el significado de los estados de evaluación.
- Buscar y evaluar siguen siendo acciones distintas. Esta spec no hace que una búsqueda registre ofertas ni dispare evaluaciones.
- La identidad de una oferta es su fuente más su dirección pública normalizada. El cargo, la empresa y la ubicación parecidos entre portales no bastan para fusionarlas.
- Repetir una evaluación crea un registro nuevo. No existe, en este incremento, una evaluación "vigente" que reemplace a las anteriores.
- No hay caducidad ni borrado automático. Lo guardado permanece hasta un cambio futuro que defina retención.
- Quedan fuera los portales reales todavía no verificados, el análisis con modelos de lenguaje, las herramientas autónomas del agente y el envío de postulaciones.
- No se abre un canal nuevo de uso. Quien ya puede completar una evaluación o registrar una oferta puede recuperarla después de reiniciar.
- Los ejemplos y los datos guardados en pruebas siguen siendo ficticios. Esta capacidad no exige cuentas personales ni credenciales de portales.
