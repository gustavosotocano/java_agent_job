# Tasks: Ejecución acotada del agente

**Input**: Design documents from `/specs/004-bound-agent-tools/`

**Prerequisites**: plan.md, spec.md, research.md, data-model.md, contracts/, quickstart.md

**Tests**: Incluidas. La constitución (principio IV) y `quickstart.md` exigen la regla de dominio sin Spring y el caso de uso con dobles.

**Organization**: Tareas agrupadas por historia de usuario para poder implementar y probar cada una por separado.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Puede ejecutarse en paralelo (otro archivo, sin depender de una tarea incompleta)
- **[Story]**: Historia a la que pertenece (`US1`, `US2`, `US3`)
- Las fases de setup, fundación y cierre no llevan etiqueta de historia

## Path Conventions

- Código en `src/main/java/com/gustavo/jobagent/`
- Pruebas en `src/test/java/com/gustavo/jobagent/`
- No se agregan controladores en `adapter/in/web/`
- No se agrega un SDK ni una migración

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Confirmar que M6 no trae un framework de agente.

- [X] T001 Dejar `pom.xml` sin dependencias nuevas: no añadir un SDK de agente, de lenguaje ni un cliente HTTP

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Partir la lista antes de llamar a una herramienta.

**⚠️ CRITICAL**: Ninguna historia empieza antes de cerrar esta fase.

- [X] T002 Crear `src/main/java/com/gustavo/jobagent/opportunities/domain/EjecucionAcotada.java`. Los únicos nombres autorizados son `buscarOfertas`, `consultarPerfil`, `consultarOferta` y `guardarEvaluacion`, comparados tal cual. El máximo es 4. Un nombre distinto corta la lista: se ejecutan solo los anteriores, el distinto queda rechazado y los posteriores quedan no ejecutados, con estado `HERRAMIENTA_NO_AUTORIZADA`. Más de 4 autorizados dejan estado `LIMITE_ALCANZADO` y el resto como no ejecutados. Cuatro o menos, todos autorizados, dejan `COMPLETADA`. La lista vacía es `COMPLETADA` con cero pasos

**Checkpoint**: La regla se puede probar sin Spring y sin casos de uso.

---

## Phase 3: User Story 1 - Solo las herramientas permitidas (Priority: P1) 🎯 MVP

**Goal**: Una búsqueda corre y un paso prohibido detiene el resto, sin registrar ni evaluar por buscar, y sin postular.

**Independent Test**: Ejecutar buscarOfertas y después `postular`. Hay un paso de búsqueda, el prohibido no corre, el cierre es `HERRAMIENTA_NO_AUTORIZADA` y no cambian las ofertas registradas ni las evaluaciones.

### Tests for User Story 1

- [X] T003 [P] [US1] Crear `src/test/java/com/gustavo/jobagent/opportunities/domain/EjecucionAcotadaTest.java`. Un nombre distinto de los cuatro corta la lista. `postular` e `iniciar sesion` no quedan entre los pasos a ejecutar. Los pasos anteriores sí. Los posteriores quedan no ejecutados
- [X] T004 [P] [US1] Crear `src/test/java/com/gustavo/jobagent/opportunities/application/service/EjecutarAgenteServiceTest.java` con el caso de búsqueda seguida de un paso no autorizado. La búsqueda se invoca una vez. No se llama a evaluar. El número de ofertas registradas y de evaluaciones no cambia. El cierre es `HERRAMIENTA_NO_AUTORIZADA`

### Implementation for User Story 1

- [X] T005 [US1] Crear `src/main/java/com/gustavo/jobagent/opportunities/application/port/in/EjecutarAgenteUseCase.java` y `src/main/java/com/gustavo/jobagent/opportunities/application/service/EjecutarAgenteService.java`. El servicio parte la lista con `EjecucionAcotada` y solo entonces llama a `BuscarOfertasUseCase`. Un paso no autorizado se anota y no se invoca

**Checkpoint**: US1 se demuestra con dobles. El tope de 4 y las otras tres herramientas llegan después.

---

## Phase 4: User Story 2 - Ver dónde terminó (Priority: P2)

**Goal**: Cinco pasos autorizados ejecutan cuatro. Un paso autorizado cierra como completada. La lista vacía no llama a nadie.

**Independent Test**: Cinco `consultarPerfil` del perfil de la ejecución ejecutan cuatro y dejan el quinto como no ejecutado, con cierre `LIMITE_ALCANZADO`. Una sola búsqueda cierra `COMPLETADA`.

### Tests for User Story 2

- [X] T006 [P] [US2] Ampliar `EjecucionAcotadaTest` con 5 nombres autorizados, con 4, con 1 y con la lista vacía. El quinto de la lista larga queda no ejecutado. Cuatro o menos cierran `COMPLETADA`
- [X] T007 [US2] Ampliar `EjecutarAgenteServiceTest` para que 5 consultas de perfil ejecuten 4 y dejen el nombre del quinto visible como no ejecutado, y para que una lista vacía no llame a buscar, consultar ni evaluar

### Implementation for User Story 2

- [X] T008 [US2] Completar en `EjecutarAgenteService` el cierre `LIMITE_ALCANZADO` y `COMPLETADA`, incluyendo en el resultado los nombres no ejecutados

**Checkpoint**: El tope y el cierre se ven sin guardar una evaluación.

---

## Phase 5: User Story 3 - Consultar y guardar sin inventar el resultado (Priority: P3)

**Goal**: El perfil desconocido no se crea. La oferta desconocida no se evalúa. La oferta registrada se evalúa con el estado del contraste.

**Independent Test**: `guardarEvaluacion` con una oferta registrada guarda el estado que devolvería `EvaluarOfertaUseCase` para ese par. Con un id que no está, crea cero evaluaciones. `consultarPerfil` con otro id no añade perfiles.

### Tests for User Story 3

- [X] T009 [US3] Ampliar `EjecutarAgenteServiceTest`. `consultarPerfil` con el id del perfil de la ejecución lo devuelve en el resumen. Con otro id, el resumen dice desconocido y no hay un perfil nuevo. `consultarOferta` con id registrado devuelve la oferta y con id ausente dice desconocida. `guardarEvaluacion` con oferta registrada guarda el estado del doble de evaluar, que es el del contraste, y con oferta ausente no llama a evaluar

### Implementation for User Story 3

- [X] T010 [US3] En `EjecutarAgenteService`, implementar `consultarPerfil` contra el perfil de la ejecución, `consultarOferta` contra `ConsultarOfertaUseCase` y `guardarEvaluacion` contra `EvaluarOfertaUseCase` solo cuando `porId` devuelve oferta. El paso no tiene campo de estado
- [X] T011 [US3] Registrar `EjecutarAgenteUseCase` en `src/main/java/com/gustavo/jobagent/configuration/OpportunitiesConfiguration.java` con los casos de uso ya existentes. No registrar un controlador

**Checkpoint**: Las cuatro herramientas respetan búsqueda, consulta y evaluación.

---

## Phase 6: Polish & Cross-Cutting Concerns

**Purpose**: Dejar el hito en la memoria y cerrar la frontera.

- [X] T012 [P] Crear `docs/casos-de-uso/UC-003-ejecutar-agente.md` y actualizar `docs/README.md` y `PLAN.md` el 2026-10-10: la ejecución acotada está entregada y el siguiente hito es M7
- [X] T013 [P] Ampliar la prueba de arquitectura del dominio para que tampoco mencione `EjecutarAgenteUseCase` ni un SDK de agente (`io.modelcontextprotocol`, `dev.langchain4j`)
- [X] T014 Ejecutar `./mvnw test` y dejar la suite en verde

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: Sin dependencias
- **Foundational (Phase 2)**: Depende del setup. Bloquea las historias
- **US1 (Phase 3)**: Depende de la fundación. Es el MVP
- **US2 (Phase 4)**: Depende de US1 porque el servicio ya ejecuta la lista partida
- **US3 (Phase 5)**: Depende de US1. Puede seguir a US2 porque comparte `EjecutarAgenteService.java`
- **Polish (Phase 6)**: Depende de las tres historias

### User Story Dependencies

- **US1**: Solo necesita la partición y la búsqueda
- **US2**: Necesita el mismo servicio para no ejecutar el quinto paso
- **US3**: Necesita consultar oferta y evaluar, que ya existen

### Parallel Opportunities

- T003 y T004 pueden escribirse juntas
- T012 y T013 pueden hacerse juntas

---

## Parallel Example: User Story 1

```bash
Task: "T003 EjecucionAcotadaTest"
Task: "T004 EjecutarAgenteServiceTest"
```

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Cerrar Phase 1 y Phase 2
2. Cerrar Phase 3
3. Parar y validar que un paso prohibido no corre y que buscar no registra ni evalúa
4. Seguir solo si el cierre es `HERRAMIENTA_NO_AUTORIZADA`

### Incremental Delivery

1. Setup + fundación: la lista se parte sin infraestructura
2. US1: búsqueda y rechazo. MVP
3. US2: el máximo de 4
4. US3: perfil, oferta y evaluación determinista
5. Phase 6: documentación, frontera y `./mvnw test`

---

## Notes

- `[P]` significa otro archivo y ninguna dependencia pendiente
- No agregar endpoints, tablas ni un SDK
- `guardarEvaluacion` no recibe un estado
- Verificar que la prueba nueva falla antes de implementar su historia
