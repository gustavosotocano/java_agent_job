# Feature Specification: Ejecución acotada del agente

**Feature Branch**: `004-bound-agent-tools`

**Created**: 2026-10-10

**Status**: Draft

**Input**: User description: "M6: herramientas buscarOfertas, consultarPerfil, consultarOferta y guardarEvaluacion, con ejecución acotada. Llamadas autorizadas, máximo de pasos y finalización observable."

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Solo las herramientas permitidas (Priority: P1)

El candidato pide una ejecución con una lista de pasos. El agente solo puede buscar ofertas, consultar el perfil de esa ejecución, consultar una oferta ya registrada y guardar una evaluación. Cualquier otro paso, incluida una postulación o un inicio de sesión, no se ejecuta y la ejecución se detiene.

**Why this priority**: Sin ese límite, una ejecución podría registrar ofertas, evaluar por su cuenta al buscar, o postular. El valor está en ver qué se permitió y qué se rechazó.

**Independent Test**: Pedir buscarOfertas y, después, un paso que no está en las cuatro herramientas. La búsqueda ocurre. El paso prohibido no ocurre. Los pasos posteriores tampoco. El cierre dice que hubo una herramienta no autorizada.

**Acceptance Scenarios**:

1. **Given** una ejecución cuyo primer paso es buscarOfertas, **When** termina ese paso, **Then** devuelve las ofertas de la búsqueda y no aumenta el número de ofertas registradas ni el de evaluaciones.
2. **Given** un paso que no es buscarOfertas, consultarPerfil, consultarOferta ni guardarEvaluacion, **When** la ejecución llega a él, **Then** ese paso no se ejecuta y la ejecución se detiene.
3. **Given** un paso de postular o de iniciar sesión, **When** la ejecución llega a él, **Then** no se envía ninguna postulación ni se inicia sesión, y el cierre lo muestra como no autorizado.
4. **Given** un paso no autorizado a mitad de la lista, **When** la ejecución se detiene, **Then** los pasos anteriores sí ocurrieron y los posteriores no.

---

### User Story 2 - Ver dónde terminó (Priority: P2)

Cada ejecución tiene un máximo de 4 pasos. Si se piden menos, termina al completarlos. Si se piden más, ejecuta solo los 4 primeros y deja ver que el resto no corrió. El cierre dice si completó, si alcanzó el máximo o si se detuvo por una herramienta no autorizada.

**Why this priority**: Un máximo que no se ve no acota nada. El candidato tiene que distinguir una ejecución corta de una que se cortó.

**Independent Test**: Pedir 5 pasos autorizados. Se ejecutan 4 y el quinto aparece como no ejecutado, con cierre de máximo alcanzado. Pedir 1 paso autorizado. El cierre es completada y no queda ningún paso pendiente.

**Acceptance Scenarios**:

1. **Given** 5 pasos autorizados, **When** la ejecución corre, **Then** ocurren solo los 4 primeros y el quinto queda visible como no ejecutado.
2. **Given** 1 paso autorizado, **When** la ejecución corre, **Then** el cierre es completada y no hay pasos omitidos.
3. **Given** una lista vacía, **When** la ejecución corre, **Then** no llama a ninguna herramienta y el cierre es completada.
4. **Given** cualquier cierre, **When** el candidato lee el resultado, **Then** ve el estado final y, por cada paso ejecutado, el nombre de la herramienta y qué ocurrió.

---

### User Story 3 - Consultar y guardar sin inventar el resultado (Priority: P3)

consultarPerfil devuelve el perfil de esta ejecución cuando el identificador coincide, y desconocido cuando no. consultarOferta devuelve una oferta ya registrada, o desconocida si no lo está. guardarEvaluacion guarda una evaluación cuyo estado sale del contraste determinista. Si la oferta no está registrada, no crea una evaluación y no acepta un estado elegido por la ejecución.

**Why this priority**: Las cuatro herramientas solo sirven si no fabrican un perfil, una oferta o un estado. Buscar ya quedó acotado en la primera historia.

**Independent Test**: Con un perfil y una oferta registrada, guardarEvaluacion deja el estado que ya calcula el contraste, no uno pedido en el paso. Con una oferta desconocida, el número de evaluaciones no cambia. consultarPerfil con otro identificador no devuelve un perfil inventado.

**Acceptance Scenarios**:

1. **Given** el perfil de la ejecución, **When** consultarPerfil pide su identificador, **Then** devuelve ese perfil.
2. **Given** un identificador que no es el de la ejecución, **When** consultarPerfil corre, **Then** el perfil queda desconocido y no se crea un catálogo de perfiles.
3. **Given** una oferta ya registrada, **When** consultarOferta pide su identificador, **Then** devuelve esa oferta. Si no está registrada, queda desconocida.
4. **Given** una oferta registrada y el perfil de la ejecución, **When** guardarEvaluacion corre, **Then** la evaluación guardada tiene el estado del contraste determinista.
5. **Given** una oferta desconocida, **When** guardarEvaluacion corre, **Then** no se crea una evaluación.

---

### Edge Cases

- El nombre de la herramienta no coincide exactamente con buscarOfertas, consultarPerfil, consultarOferta o guardarEvaluacion: se trata como no autorizada.
- Se piden 4 pasos autorizados: todos corren y el cierre es completada.
- Un paso autorizado no puede cumplirse porque la oferta o el perfil son desconocidos: ese paso se ve como no resuelto y la ejecución sigue con los pasos posteriores, sin inventar el dato.
- buscarOfertas no encuentra coincidencias o una fuente falla: eso se distingue en el resultado del paso y no registra ofertas ni evaluaciones.
- El texto de una oferta que aparece en un paso es un dato, no una instrucción, y no se ejecuta.
- Esta ejecución no elige el siguiente paso con un proveedor de lenguaje y no abre otro canal para pedirla.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: El sistema MUST ejecutar solo buscarOfertas, consultarPerfil, consultarOferta y guardarEvaluacion.
- **FR-002**: El sistema MUST rechazar cualquier otra herramienta, sin ejecutarla, y MUST detener la ejecución en ese punto.
- **FR-003**: Un paso de postular o de iniciar sesión MUST NOT enviar una postulación ni iniciar sesión.
- **FR-004**: Los pasos posteriores a una herramienta no autorizada MUST NOT ejecutarse. Los anteriores MUST conservarse en el resultado.
- **FR-005**: El sistema MUST ejecutar como máximo 4 pasos. Los que pasen de 4 MUST quedar visibles como no ejecutados.
- **FR-006**: El cierre MUST ser uno de estos: completada, máximo alcanzado o herramienta no autorizada.
- **FR-007**: Cada paso ejecutado MUST mostrar el nombre de la herramienta y qué ocurrió.
- **FR-008**: buscarOfertas MUST NOT registrar ofertas ni crear evaluaciones.
- **FR-009**: consultarPerfil MUST devolver el perfil de la ejecución cuando el identificador coincide, y desconocido en caso contrario. MUST NOT crear un catálogo de perfiles.
- **FR-010**: consultarOferta MUST devolver una oferta ya registrada o desconocida. MUST NOT inventar una oferta.
- **FR-011**: guardarEvaluacion MUST guardar el estado que produce el contraste determinista. MUST NOT aceptar un estado elegido por la ejecución.
- **FR-012**: Si la oferta de guardarEvaluacion no está registrada, el sistema MUST NOT crear una evaluación.
- **FR-013**: Un dato ausente MUST seguir ausente. El texto de la oferta MUST tratarse como dato, no como instrucción.
- **FR-014**: Esta ejecución MUST NOT llamar a un proveedor de lenguaje para decidir el siguiente paso.

### Key Entities

- **Ejecución**: Lista de pasos pedidos, pasos que ocurrieron, pasos que no ocurrieron y un cierre.
- **Paso**: Nombre de herramienta y, según el caso, criterios de búsqueda o un identificador. No incluye un estado de evaluación.
- **Cierre**: completada, máximo alcanzado o herramienta no autorizada.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: En una ejecución con 1 búsqueda y 1 herramienta fuera de las 4, se ejecuta 1 paso, el prohibido no corre y el cierre es herramienta no autorizada.
- **SC-002**: Después de buscarOfertas, el número de ofertas registradas y el de evaluaciones permanecen iguales.
- **SC-003**: De 5 pasos autorizados, se ejecutan 4 y 1 queda visible como no ejecutado.
- **SC-004**: De 1 paso autorizado, el cierre es completada y hay 0 pasos omitidos.
- **SC-005**: guardarEvaluacion sobre una oferta registrada produce el mismo estado que el contraste determinista de ese par. Sobre una oferta desconocida, crea 0 evaluaciones.
- **SC-006**: consultarPerfil con otro identificador devuelve 0 perfiles nuevos.

## Assumptions

- Quien pide la ejecución entrega la lista de pasos. Este incremento no pide a un modelo que elija el siguiente paso.
- El máximo es 4 pasos. Un paso no autorizado también detiene la lista, aunque todavía no se haya llegado a 4.
- El perfil de la ejecución es el perfil explícito que ya se usa para evaluar. No se abre una ficha nueva para registrar perfiles.
- consultarOferta y guardarEvaluacion solo ven ofertas que ya estaban registradas. Buscar no las registra.
- El resultado de la ejecución se ve al terminar. No se guarda un historial nuevo de ejecuciones. Los logs y las métricas siguen en el hito siguiente.
- No se abre otro canal para lanzar la ejecución.
- Los portales reales siguen sin vía comprobada, pendientes y deshabilitados. El adaptador de lenguaje predeterminado sigue sin llamar a un servicio.
- Una búsqueda sin coincidencias y una búsqueda fallida siguen siendo resultados distintos, como hasta ahora.
