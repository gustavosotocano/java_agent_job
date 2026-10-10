# Tasks: Verificar acceso a portales públicos

**Input**: Design documents from `/specs/002-verify-portal-access/`

**Prerequisites**: plan.md, spec.md, research.md, data-model.md, contracts/, quickstart.md

**Tests**: Incluidas. La constitución (principio IV) y `quickstart.md` exigen pruebas de dominio, de caso de uso y de persistencia. En cada historia se escriben primero y deben fallar antes de la implementación.

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
- No se agregan adaptadores de LinkedIn, Indeed ni Glassdoor

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Confirmar que M4.1 no trae un cliente de portal. La pila de M4 ya está instalada.

- [X] T001 Dejar `pom.xml` sin dependencias nuevas: no añadir cliente HTTP, SDK de LinkedIn, Indeed o Glassdoor, ni un agregador de ofertas

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Sembrar los tres dictámenes sin tocar el catálogo de fuentes de M4.

**⚠️ CRITICAL**: Ninguna historia empieza antes de cerrar esta fase.

- [X] T002 Crear `src/main/resources/db/migration/V4__dictamen_acceso.sql` con la tabla `dictamen_acceso`. `fuente_id` solo admite `LINKEDIN`, `INDEED` o `GLASSDOOR`. `via_comprobada` es obligatorio y en la semilla es falso. `motivo` es obligatorio cuando `via_comprobada` es falso. `clase_acceso`, `cobertura`, `limites` y `actualidad` quedan nulos cuando la vía no está comprobada; `clase_acceso`, si alguna vez es verdadero, solo admite `LISTADO_PUBLICO`, `FEED`, `PROVEEDOR_AUTORIZADO` o `BUSQUEDA_PUBLICA_INDEXADA`. `verificado_en` es obligatorio, con zona horaria, y la semilla usa `2026-10-10`. Prohibido guardar `0`, cadena vacía o un relleno en un campo desconocido. Sembrar `LINKEDIN` con motivo "La API oficial publicada es de socios para publicar avisos y postulaciones, no para buscar. No hay acuerdo ni una API pública de búsqueda"; `INDEED` con motivo "La búsqueda pública de publishers está retirada. Las APIs actuales de socios envían avisos y postulaciones hacia Indeed y exigen alta"; `GLASSDOOR` con motivo "La API de socios que exponía empleos ya no atiende y no acepta altas nuevas. La vía empresarial reparte avisos desde un ATS, no permite buscar". No ejecutar `UPDATE` sobre `fuente_configurada`

**Checkpoint**: Flyway aplica V4. `SIMULADA` sigue `DISPONIBLE` y habilitada. Los tres portales siguen `PENDIENTE` y deshabilitados. Todavía no hay caso de uso de lectura.

---

## Phase 3: User Story 1 - Saber qué portales se pueden consultar (Priority: P1) 🎯 MVP

**Goal**: LinkedIn, Indeed y Glassdoor tienen un dictamen de vía no comprobada que se vuelve a leer igual después de reiniciar.

**Independent Test**: Listar los dictámenes sin buscar. Hay tres, ninguno con vía comprobada, y `SIMULADA` no aparece. Cerrar el contexto, abrir otro contra la misma base y leer los mismos tres.

### Tests for User Story 1

- [X] T003 [P] [US1] Escribir primero, en rojo, `src/test/java/com/gustavo/jobagent/opportunities/domain/DictamenAccesoTest.java`: rechaza `fuente_id` vacío o distinto de `LINKEDIN`, `INDEED` y `GLASSDOOR`; rechaza vía no comprobada sin motivo o con motivo en blanco; rechaza vía no comprobada con `clase_acceso`, `cobertura`, `limites` o `actualidad` informados; rechaza vía comprobada a la que le falte alguna de esas cuatro descripciones; rechaza vía comprobada cuyo motivo diga que hace falta cuenta personal o iniciar sesión
- [X] T004 [P] [US1] Escribir primero, en rojo, `src/test/java/com/gustavo/jobagent/opportunities/application/service/ConsultarDictamenAccesoServiceTest.java` con un doble: `listar` devuelve `LINKEDIN`, `INDEED` y `GLASSDOOR` en ese orden; `porFuente("SIMULADA")` vuelve vacío; no registra ofertas ni evaluaciones
- [X] T005 [P] [US1] Escribir primero, en rojo, `src/test/java/com/gustavo/jobagent/opportunities/adapter/out/persistence/DictamenAccesoPostgresTest.java`: tras arrancar y tras un contexto nuevo contra el mismo contenedor, los tres dictámenes siguen con `viaComprobada` falso, motivo no vacío, clase de acceso, cobertura, límites y actualidad nulos, y `verificadoEn` en `2026-10-10`

### Implementation for User Story 1

- [X] T006 [P] [US1] Crear el value object `src/main/java/com/gustavo/jobagent/opportunities/domain/DictamenAcceso.java` con las reglas de T003. `claseAcceso` solo puede ser `LISTADO_PUBLICO`, `FEED`, `PROVEEDOR_AUTORIZADO` o `BUSQUEDA_PUBLICA_INDEXADA`, y solo cuando la vía está comprobada
- [X] T007 [P] [US1] Mapear `dictamen_acceso` en `src/main/java/com/gustavo/jobagent/opportunities/adapter/out/persistence/jpa/DictamenAccesoJpaEntity.java` sin rellenar con `0` ni con texto vacío un campo nulo
- [X] T008 [US1] Declarar `src/main/java/com/gustavo/jobagent/opportunities/application/port/out/DictamenAccesoRepository.java` con `listar` y `porFuente`
- [X] T009 [US1] Crear `src/main/java/com/gustavo/jobagent/opportunities/application/port/in/ConsultarDictamenAcceso.java` y `src/main/java/com/gustavo/jobagent/opportunities/application/service/ConsultarDictamenAccesoService.java`. `listar` ordena `LINKEDIN`, `INDEED`, `GLASSDOOR`. `porFuente("SIMULADA")` devuelve vacío
- [X] T010 [US1] Implementar `src/main/java/com/gustavo/jobagent/opportunities/adapter/out/persistence/JpaDictamenAccesoRepository.java` y el repositorio Spring Data junto a la entidad de T007. La lectura no llama a ningún portal
- [X] T011 [US1] Registrar el caso de uso y el adaptador en `src/main/java/com/gustavo/jobagent/configuration/OpportunitiesConfiguration.java` cuando haya datasource. No declarar ahí un `BuscarOfertasPort` de portal real

**Checkpoint**: US1 se prueba sola. La búsqueda todavía no se toca.

---

## Phase 4: User Story 2 - Buscar solo en portales con acceso comprobado (Priority: P2)

**Goal**: La búsqueda sigue consultando solo `SIMULADA`. Los tres portales no aportan ofertas y no cambian de estado.

**Independent Test**: `POST /api/ofertas/busquedas` con criterios que la fuente simulada resuelve. `fuentesConsultadas` contiene `SIMULADA` y no contiene `LINKEDIN`, `INDEED` ni `GLASSDOOR`. Los conteos de `oferta` y `evaluacion` no cambian.

### Tests for User Story 2

- [X] T012 [P] [US2] Escribir primero, en rojo, `src/test/java/com/gustavo/jobagent/opportunities/adapter/in/web/BusquedaSinPortalesRealesPostgresTest.java`: el `POST /api/ofertas/busquedas` no aumenta filas de `oferta` ni de `evaluacion`; el cuerpo sigue siendo ofertas, fuentes consultadas y advertencias; `fuentesConsultadas` incluye `SIMULADA` y excluye `LINKEDIN`, `INDEED` y `GLASSDOOR`; no hay advertencia solo por estar pendientes
- [X] T013 [P] [US2] Escribir primero, en rojo, `src/test/java/com/gustavo/jobagent/opportunities/adapter/out/PortalesRealesNoRegistradosTest.java`: el contexto expone un solo `BuscarOfertasPort`, el de `SIMULADA`, y `fuente_configurada` conserva `SIMULADA` `DISPONIBLE` habilitada y los tres portales `PENDIENTE` deshabilitados

### Implementation for User Story 2

- [X] T014 [US2] En `src/main/java/com/gustavo/jobagent/configuration/OpportunitiesConfiguration.java`, dejar `fuenteOfertasSimuladaAdapter` como único `BuscarOfertasPort`. No crear clases en `adapter/out/` para LinkedIn, Indeed o Glassdoor y no cambiar las filas de `fuente_configurada`

**Checkpoint**: US1 y US2 funcionan por separado. Cero portales reales consultables.

---

## Phase 5: User Story 3 - Aislar el fallo de un portal (Priority: P3)

**Goal**: Si una fuente habilitada falla y otra responde, la búsqueda devuelve los resultados válidos y una advertencia de la fuente fallida. Buscar no registra, no evalúa, no postula y no inicia sesión.

**Independent Test**: Una fuente que responde y otra habilitada que lanza error. Hay ofertas de la que respondió y una advertencia de la que falló. El número de ofertas registradas y de evaluaciones no cambia.

### Tests for User Story 3

- [X] T015 [P] [US3] Escribir primero, en rojo, las aserciones nuevas en `src/test/java/com/gustavo/jobagent/opportunities/application/service/BuscarOfertasServiceTest.java`: una fuente que responde y otra que falla dejan ofertas de la primera y una advertencia de la segunda; el resultado vacío sigue distinto de la búsqueda fallida; `buscar` no registra ofertas ni evaluaciones
- [X] T016 [P] [US3] Escribir primero, en rojo, `src/test/java/com/gustavo/jobagent/opportunities/application/service/BuscarOfertasNoPostulaTest.java`: `BuscarOfertasService` no expone una operación de postulación ni de login, y una búsqueda no construye una oferta de `LINKEDIN`, `INDEED` o `GLASSDOOR`

### Implementation for User Story 3

- [X] T017 [US3] Conservar en `src/main/java/com/gustavo/jobagent/opportunities/application/service/BuscarOfertasService.java` el fallo parcial, la distinción entre vacío y fallo, y la prohibición de registrar o evaluar. No añadir llamadas a portales, postulaciones ni login

**Checkpoint**: Las tres historias funcionan. Ningún portal real se consulta.

---

## Phase 6: Polish & Cross-Cutting Concerns

**Purpose**: Cerrar la memoria del proyecto y la frontera del dominio.

- [X] T018 [P] Actualizar `docs/casos-de-uso/UC-001-buscar-ofertas.md`, `docs/README.md` y `PLAN.md`: M4.1 deja dictamen de vía no comprobada para LinkedIn, Indeed y Glassdoor; no hay fuente real nueva; las tres siguen pendientes y deshabilitadas; la simulada sigue disponible. No dar por hecha una integración ni el LLM
- [X] T019 [P] Extender `src/test/java/com/gustavo/jobagent/opportunities/ArchitectureBoundariesTest.java` para que `opportunities.domain` tampoco importe un cliente HTTP ni un SDK de portal
- [X] T020 Ejecutar `./mvnw test` desde la raíz del repositorio y dejar en verde los escenarios de `specs/002-verify-portal-access/quickstart.md`

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: sin dependencias
- **Foundational (Phase 2)**: depende del setup y bloquea todas las historias
- **User Stories (Phase 3+)**: dependen de la fundación. US2 no necesita leer dictámenes para afirmar que la búsqueda no consulta portales. US3 no necesita US2 para aislar un fallo entre fuentes falsas
- **Polish (Phase 6)**: depende de las historias que se quieran dar por cerradas

### User Story Dependencies

- **User Story 1 (P1)**: empieza tras la fundación. No busca en portales
- **User Story 2 (P2)**: empieza tras la fundación. No crea dictámenes nuevos. T014 edita `OpportunitiesConfiguration.java` después de T011
- **User Story 3 (P3)**: empieza tras la fundación. No escribe `dictamen_acceso`, `oferta` ni `evaluacion`

### Within Each User Story

- Las pruebas de la historia se escriben y fallan antes de su implementación
- El value object y la entidad de US1 pueden ir en paralelo
- El adaptador espera al puerto y a la entidad
- La historia queda verificada antes de pasar a la siguiente prioridad

### Parallel Opportunities

- T003, T004 y T005 pueden ir juntos
- T006 y T007 pueden ir juntos
- T012 y T013 pueden ir juntos
- T015 y T016 pueden ir juntos
- T018 y T019 pueden ir juntos

---

## Parallel Example: User Story 1

```bash
# Pruebas de US1, juntas y en rojo:
Task: "T003 DictamenAccesoTest"
Task: "T004 ConsultarDictamenAccesoServiceTest"
Task: "T005 DictamenAccesoPostgresTest"

# Modelo y tabla mapeada, juntos:
Task: "T006 DictamenAcceso"
Task: "T007 DictamenAccesoJpaEntity"
```

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Cerrar Phase 1 y Phase 2
2. Cerrar Phase 3
3. Parar y validar que los tres dictámenes se leen igual tras reiniciar
4. Seguir solo si los tres siguen con vía no comprobada y campos desconocidos en nulo

### Incremental Delivery

1. Setup + fundación: la semilla existe y el catálogo de M4 no cambia
2. US1: el candidato puede leer el dictamen. MVP
3. US2: la búsqueda no consulta portales reales
4. US3: un fallo de fuente no borra el resto ni registra ofertas
5. Phase 6: documentación, frontera de arquitectura y `./mvnw test`

### Parallel Team Strategy

1. Todos cierran setup y fundación
2. Después: una persona por historia. `OpportunitiesConfiguration.java` se integra al terminar US1 y otra vez en US2, no a la vez
3. Cada historia se demuestra con su prueba independiente

---

## Notes

- `[P]` significa otro archivo y ninguna dependencia pendiente
- No agregar endpoints ni adaptadores de portal
- Un dictamen no comprobado persiste los cuatro descriptores como nulos, nunca como cero ni como texto vacío
- `fuente_configurada` no se actualiza en este incremento
- Verificar que la prueba nueva falla antes de implementar su historia
