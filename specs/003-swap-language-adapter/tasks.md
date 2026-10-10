# Tasks: Adaptador de lenguaje intercambiable

**Input**: Design documents from `/specs/003-swap-language-adapter/`

**Prerequisites**: plan.md, spec.md, research.md, data-model.md, contracts/, quickstart.md

**Tests**: Incluidas. La constitución (principio IV) y `quickstart.md` exigen pruebas de dominio, de caso de uso, de adaptador y de persistencia. En cada historia se escriben primero.

**Organization**: Tareas agrupadas por historia de usuario para poder implementar y probar cada una por separado.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Puede ejecutarse en paralelo (otro archivo, sin depender de una tarea incompleta)
- **[Story]**: Historia a la que pertenece (`US1`, `US2`, `US3`)
- Las fases de setup, fundación y cierre no llevan etiqueta de historia

## Path Conventions

- Código en `src/main/java/com/gustavo/jobagent/`
- Pruebas en `src/test/java/com/gustavo/jobagent/`
- Migraciones en `src/main/resources/db/migration/`
- No se agregan controladores en `adapter/in/web/`
- No se agrega un SDK de modelo de lenguaje

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Confirmar que M5 no trae un cliente de proveedor. La pila de M4 ya está instalada.

- [X] T001 Dejar `pom.xml` sin dependencias nuevas: no añadir un SDK de modelo de lenguaje ni un cliente HTTP

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Dejar el agregado con una lista de observaciones vacía y la tabla hija, sin cambiar el cálculo.

**⚠️ CRITICAL**: Ninguna historia empieza antes de cerrar esta fase.

- [X] T002 Crear `src/main/resources/db/migration/V5__evaluacion_observacion.sql` con la tabla `evaluacion_observacion`. `evaluacion_id` es obligatorio y referencia `evaluacion(id)`. `orden` es obligatorio y, con `evaluacion_id`, forma la clave primaria. `texto` y `fragmento` son obligatorios, con `CHECK` de longitud recortada mayor que 0. Solo inserción: la migración no hace `UPDATE` de `evaluacion` ni de sus hijas ya existentes
- [X] T003 Extender `src/main/java/com/gustavo/jobagent/opportunities/domain/EvaluacionOferta.java` con `List<ObservacionPropuesta> observaciones`. `evaluar` la deja vacía. `conObservaciones` sustituye solo esa lista y conserva `estado`, `coincidencias`, `brechas`, `datosPendientes`, identificadores y `fechaEvaluacion`. Actualizar los sitios que construyen el record para pasar la lista

**Checkpoint**: El cálculo determinista sigue igual y la tabla nueva existe. Todavía no hay puerto ni filtro.

---

## Phase 3: User Story 1 - La compatibilidad calculada no cambia (Priority: P1) 🎯 MVP

**Goal**: El mismo perfil y la misma oferta producen el mismo estado y el mismo desglose con propuesta vacía, propuesta válida o puerto caído.

**Independent Test**: Evaluar tres veces el mismo par contra un puerto vacío, uno que devuelve una observación aceptable y uno que falla. El estado y el desglose coinciden y las tres evaluaciones se guardan.

### Tests for User Story 1

- [X] T004 [P] [US1] Crear `src/test/java/com/gustavo/jobagent/opportunities/domain/EvaluacionOfertaPropuestaTest.java`. `evaluar` deja `observaciones` vacía. `conObservaciones` no cambia `estado`, `coincidencias`, `brechas` ni `datosPendientes`
- [X] T005 [P] [US1] Crear `src/test/java/com/gustavo/jobagent/opportunities/application/service/EvaluarOfertaPropuestaServiceTest.java`. Con el mismo perfil y la misma oferta, un puerto disponible vacío, uno con una observación cuyo fragmento está en la oferta y uno que lanza un fallo dejan el mismo `estado`, las mismas coincidencias, brechas y datos pendientes, y el repositorio guarda las tres

### Implementation for User Story 1

- [X] T006 [P] [US1] Crear `src/main/java/com/gustavo/jobagent/opportunities/domain/ObservacionPropuesta.java`. `texto` y `fragmento` se recortan y rechazan nulo o blanco
- [X] T007 [P] [US1] Crear `src/main/java/com/gustavo/jobagent/opportunities/domain/ResultadoPropuesta.java` con variante disponible (lista, quizá vacía) y no disponible con motivo `FALLO`, `TIEMPO_AGOTADO` o `ILEGIBLE`
- [X] T008 [US1] Crear `src/main/java/com/gustavo/jobagent/opportunities/application/port/out/PropuestaLenguajePort.java` con `proponer(oferta, perfil, limite)` que devuelve `ResultadoPropuesta`
- [X] T009 [US1] Actualizar `src/main/java/com/gustavo/jobagent/opportunities/application/service/EvaluarOfertaService.java` para calcular primero con `EvaluacionOferta.evaluar`, pedir la propuesta y guardar siempre. Un `NoDisponible`, una excepción o un tiempo mayor que el límite adjuntan cero observaciones. El límite que recibe el servicio en producción es de 2 segundos. `conObservaciones` es el único modo de adjuntarlas

**Checkpoint**: US1 se demuestra con el doble del puerto. El adaptador predeterminado y la base llegan en las historias siguientes.

---

## Phase 4: User Story 2 - Ver la propuesta aparte del cálculo (Priority: P2)

**Goal**: Solo entra una observación citada. Lo inventado se descarta. Al reiniciar se lee el mismo número de observaciones.

**Independent Test**: Dos candidatas, una con fragmento presente y otra con un número que no está en el corpus. Se guarda una. Un texto ilegible guarda cero. Otro contexto contra la misma base lee ese resultado.

### Tests for User Story 2

- [X] T010 [P] [US2] Crear `src/test/java/com/gustavo/jobagent/opportunities/domain/PropuestaTest.java`. Se acepta la observación cuyo `fragmento` está en el corpus. Se rechaza la que trae un número ausente, una modalidad ausente (`REMOTE`, `HYBRID`, `ONSITE`, `remoto`, `hibrido`, `híbrido`, `presencial`), un estado (`COMPATIBLE`, `COMPATIBLE_CON_BRECHAS`, `NO_COMPATIBLE`) o un token técnico de mayúscula inicial y al menos tres caracteres ausente del corpus. Si ninguna pasa, la lista queda vacía. Un salario ausente no se convierte en `0`
- [X] T011 [P] [US2] Crear `src/test/java/com/gustavo/jobagent/opportunities/adapter/out/lenguaje/PropuestaTextoAdapterTest.java`. Un bloque `texto:` y `fragmento:` se lee. Un texto vacío, un bloque incompleto o un trozo a medias devuelve `ILEGIBLE` y cero candidatas
- [X] T012 [US2] Crear `src/test/java/com/gustavo/jobagent/opportunities/adapter/out/persistence/EvaluacionPropuestaPostgresTest.java`. Guardar una evaluación con una observación aceptada y otra con cero. Abrir otro contexto y leer el mismo estado y el mismo número de observaciones. Una segunda evaluación del mismo par no reescribe la primera

### Implementation for User Story 2

- [X] T013 [US2] Crear `src/main/java/com/gustavo/jobagent/opportunities/domain/Propuesta.java` con `aceptar(candidatas, oferta, perfil)` según las reglas de T010. El corpus usa título, empresa, descripción, ubicación, modalidad, salario publicado si existe y tecnologías de la oferta, y nombre, título, tecnologías, modalidades, ubicación, expectativa salarial y años de experiencia del perfil cuando existen
- [X] T014 [US2] Crear `src/main/java/com/gustavo/jobagent/opportunities/adapter/out/lenguaje/PropuestaTextoAdapter.java`. Interpreta bloques `texto:` y `fragmento:`. Si no se puede leer, devuelve `ILEGIBLE` y no conserva un bloque a medias
- [X] T015 [US2] Hacer que `EvaluarOfertaService` filtre las candidatas con `Propuesta.aceptar` antes de `conObservaciones`
- [X] T016 [US2] Mapear `evaluacion_observacion` en el adaptador JPA (`EvaluacionJpaEntity` y `JpaEvaluacionOfertaRepository`) y en `InMemoryEvaluacionOfertaRepository`, de solo inserción, conservando el orden. Cero filas significan propuesta vacía

**Checkpoint**: La propuesta aceptada se distingue del desglose calculado y sobrevive al reinicio.

---

## Phase 5: User Story 3 - Sustituir quién propone (Priority: P3)

**Goal**: Dos adaptadores locales. El predeterminado no llama a un servicio. El estado calculado no cambia al sustituirlos. Un puerto lento no deja la evaluación a medias.

**Independent Test**: El mismo par con el adaptador vacío y con el de texto conserva el estado. Sin propiedad de adaptador no hay llamada de red. Un puerto que tarda más que el límite guarda la evaluación con cero observaciones.

### Tests for User Story 3

- [X] T017 [P] [US3] Crear `src/test/java/com/gustavo/jobagent/opportunities/adapter/out/lenguaje/PropuestaVaciaAdapterTest.java`. Devuelve disponible con lista vacía y no abre una conexión
- [X] T018 [P] [US3] Ampliar `EvaluarOfertaPropuestaServiceTest` para el mismo par con el adaptador vacío y con `PropuestaTextoAdapter`: el `estado` coincide y solo cambia el número de observaciones. Un puerto que supera el límite guarda la evaluación con cero observaciones
- [X] T019 [US3] Crear `src/test/java/com/gustavo/jobagent/opportunities/application/service/BuscarOfertasNoProponeTest.java`. Declarar que `BuscarOfertasService` no usa `PropuestaLenguajePort` ni métodos de postular o iniciar sesión

### Implementation for User Story 3

- [X] T020 [P] [US3] Crear `src/main/java/com/gustavo/jobagent/opportunities/adapter/out/lenguaje/PropuestaVaciaAdapter.java`. Devuelve disponible y lista vacía, sin red
- [X] T021 [US3] Registrar en `src/main/java/com/gustavo/jobagent/configuration/OpportunitiesConfiguration.java` el adaptador `vacia` como predeterminado y el adaptador `texto` solo cuando `jobagent.lenguaje.adaptador=texto`, leyendo `jobagent.lenguaje.texto`. Pasar al caso de uso un límite de 2 segundos. No registrar un controlador ni un cliente de proveedor

**Checkpoint**: Se puede sustituir el adaptador sin cambiar el contraste ni salir del proceso.

---

## Phase 6: Polish & Cross-Cutting Concerns

**Purpose**: Documentar el hito y cerrar la frontera de arquitectura.

- [X] T022 [P] Actualizar `docs/casos-de-uso/UC-002-evaluar-oferta.md`, `docs/README.md` y `PLAN.md` el 2026-10-10: la propuesta es aparte del cálculo, el adaptador predeterminado no llama a un servicio y los portales reales siguen como en M4.1
- [X] T023 [P] Ampliar la prueba de arquitectura del dominio para que `src/main/java/com/gustavo/jobagent/opportunities/domain` tampoco mencione un SDK de lenguaje (`com.openai`, `dev.langchain4j`, `com.anthropic`)
- [X] T024 Ejecutar `./mvnw test` y dejar la suite en verde

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: Sin dependencias
- **Foundational (Phase 2)**: Depende del setup. Bloquea las historias
- **US1 (Phase 3)**: Depende de la fundación. Es el MVP
- **US2 (Phase 4)**: Depende de US1 porque filtra y persiste lo que el caso de uso ya adjunta
- **US3 (Phase 5)**: Depende de US1 y del adaptador de texto de US2
- **Polish (Phase 6)**: Depende de las tres historias

### User Story Dependencies

- **US1**: Solo necesita el agregado con lista vacía
- **US2**: Necesita el caso de uso de US1 para guardar lo aceptado
- **US3**: Necesita los dos adaptadores y el límite del caso de uso

### Parallel Opportunities

- T004 y T005 pueden escribirse juntas
- T006 y T007 pueden escribirse juntas
- T010 y T011 pueden escribirse juntas
- T017 y T018 pueden escribirse juntas
- T022 y T023 pueden hacerse juntas

---

## Parallel Example: User Story 1

```bash
# Pruebas de US1, juntas:
Task: "T004 EvaluacionOfertaPropuestaTest"
Task: "T005 EvaluarOfertaPropuestaServiceTest"

# Tipos de dominio, juntos:
Task: "T006 ObservacionPropuesta"
Task: "T007 ResultadoPropuesta"
```

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Cerrar Phase 1 y Phase 2
2. Cerrar Phase 3
3. Parar y validar que el estado calculado coincide en los tres escenarios
4. Seguir solo si un fallo del puerto igual guarda la evaluación

### Incremental Delivery

1. Setup + fundación: la lista existe y el cálculo no cambió
2. US1: el caso de uso aísla el fallo. MVP
3. US2: el filtro y el reinicio
4. US3: el intercambio local y el límite de 2 segundos
5. Phase 6: documentación, frontera y `./mvnw test`

### Parallel Team Strategy

1. Todos cierran setup y fundación
2. US2 y US3 no empiezan antes de US1. `EvaluarOfertaService.java` se toca en US1 y otra vez en US2, no a la vez
3. Cada historia se demuestra con su prueba independiente

---

## Notes

- `[P]` significa otro archivo y ninguna dependencia pendiente
- No agregar endpoints ni un SDK de lenguaje
- `evaluar` no recibe el puerto
- Una propuesta vacía es un resultado válido, no un error
- Verificar que la prueba nueva falla antes de implementar su historia
