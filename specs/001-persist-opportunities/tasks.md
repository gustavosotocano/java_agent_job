# Tasks: Persistencia de oportunidades

**Input**: Design documents from `/specs/001-persist-opportunities/`

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

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Dependencias y arranque local de PostgreSQL, sin cambiar el dominio.

- [X] T001 [P] Agregar en `pom.xml`, sin versión explícita, `spring-boot-starter-data-jpa`, `spring-boot-starter-flyway`, `org.flywaydb:flyway-database-postgresql` si el starter no lo trae, `org.postgresql:postgresql` en runtime, y en test `spring-boot-testcontainers`, `org.testcontainers:testcontainers-junit-jupiter` y `org.testcontainers:testcontainers-postgresql`
- [X] T002 [P] Crear `docker-compose.yml` con la imagen `postgres:17`, base `jobagent`, usuario `jobagent`, contraseña `jobagent` y puerto `5432`
- [X] T003 [P] Configurar en `src/main/resources/application.yml` el datasource desde `SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME` y `SPRING_DATASOURCE_PASSWORD`, y Flyway en `classpath:db/migration`

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: El contexto de Spring sigue arrancando cuando JPA exija un datasource.

**⚠️ CRITICAL**: Ninguna historia empieza antes de cerrar esta fase.

- [X] T004 [P] Crear `src/test/java/com/gustavo/jobagent/TestcontainersConfiguration.java` con `@TestConfiguration`, un `@Bean` `@ServiceConnection` y `PostgreSQLContainer` de `postgres:17`
- [X] T005 Importar `TestcontainersConfiguration` en `src/test/java/com/gustavo/jobagent/JobAgentApplicationTests.java` para que `contextLoads` use ese contenedor
- [X] T006 [P] Crear `src/main/java/com/gustavo/jobagent/opportunities/adapter/out/persistence/jpa/package-info.java` declarando que las entidades JPA solo viven en ese paquete

**Checkpoint**: `./mvnw test` puede cargar el contexto contra PostgreSQL. Todavía no hay tablas de negocio.

---

## Phase 3: User Story 1 - Recuperar una evaluación después de reiniciar (Priority: P1) 🎯 MVP

**Goal**: Una evaluación completada se recupera igual después de reiniciar, con su evidencia, y una segunda evaluación no borra la primera.

**Independent Test**: Evaluar, cerrar el contexto, abrir otro contra el mismo contenedor y leer la evaluación completa. El salario ausente sigue ausente. Dos evaluaciones del mismo par siguen siendo dos.

### Tests for User Story 1

- [X] T007 [P] [US1] Escribir primero, en rojo, `src/test/java/com/gustavo/jobagent/opportunities/adapter/out/persistence/EvaluacionReinicioPostgresTest.java`: misma evaluación tras un contexto nuevo (id, estado, coincidencias, brechas, datos pendientes, copia de perfil y copia de oferta); salario de evidencia nulo; la segunda evaluación tiene otro id y no modifica la primera
- [X] T008 [P] [US1] Extender `src/test/java/com/gustavo/jobagent/opportunities/application/service/EvaluarOfertaServiceTest.java` para afirmar que dos llamadas `evaluar` del mismo par guardan dos agregados y el id del primero no cambia

### Implementation for User Story 1

- [X] T009 [P] [US1] Crear `src/main/resources/db/migration/V1__evaluacion.sql` con `evaluacion` (solo inserciones; `estado` en `COMPATIBLE`, `COMPATIBLE_CON_BRECHAS` o `NO_COMPATIBLE`; `id_oferta` guarda `OfertaLaboral.idFuente()`; `id_perfil` guarda `PerfilProfesional.id()`), `evaluacion_coincidencia` (`aspecto`, `detalle`, `evidencia`), `evaluacion_brecha` (`aspecto`, `detalle`, `es_bloqueante`), `evaluacion_dato_pendiente` (`aspecto`, `pregunta`) y las copias `evaluacion_oferta_evidencia` y `evaluacion_perfil_evidencia`. `aspecto` admite `TECNOLOGIA`, `MODALIDAD`, `UBICACION`, `SALARIO`, `EXPERIENCIA`. El orden de las listas se conserva
- [X] T010 [P] [US1] Mapear `evaluacion` en `src/main/java/com/gustavo/jobagent/opportunities/adapter/out/persistence/jpa/EvaluacionJpaEntity.java` sin operación de actualización
- [X] T011 [P] [US1] Mapear `evaluacion_coincidencia` en `src/main/java/com/gustavo/jobagent/opportunities/adapter/out/persistence/jpa/EvaluacionCoincidenciaJpaEntity.java`
- [X] T012 [P] [US1] Mapear `evaluacion_brecha` en `src/main/java/com/gustavo/jobagent/opportunities/adapter/out/persistence/jpa/EvaluacionBrechaJpaEntity.java`
- [X] T013 [P] [US1] Mapear `evaluacion_dato_pendiente` en `src/main/java/com/gustavo/jobagent/opportunities/adapter/out/persistence/jpa/EvaluacionDatoPendienteJpaEntity.java`
- [X] T014 [P] [US1] Mapear `evaluacion_oferta_evidencia` en `src/main/java/com/gustavo/jobagent/opportunities/adapter/out/persistence/jpa/EvaluacionOfertaEvidenciaJpaEntity.java`, con `fuente_id`, `url_normalizada` nula si no se pudo calcular, y salario nulo si la oferta no lo traía
- [X] T015 [P] [US1] Mapear `evaluacion_perfil_evidencia` en `src/main/java/com/gustavo/jobagent/opportunities/adapter/out/persistence/jpa/EvaluacionPerfilEvidenciaJpaEntity.java`, incluida expectativa salarial nula si el perfil no la define
- [X] T016 [US1] Ampliar `src/main/java/com/gustavo/jobagent/opportunities/application/port/out/EvaluacionOfertaRepository.java` para guardar y leer `EvaluacionAlmacenada` sin quitar `guardar`, `buscarPorId`, `buscarPorPerfil` ni `buscarPorOferta`
- [X] T017 [US1] Actualizar `src/main/java/com/gustavo/jobagent/opportunities/adapter/out/persistence/InMemoryEvaluacionOfertaRepository.java` para cumplir el puerto ampliado en T016
- [X] T018 [US1] Implementar el adaptador `src/main/java/com/gustavo/jobagent/opportunities/adapter/out/persistence/JpaEvaluacionOfertaRepository.java` contra el puerto de T016: `guardar` solo inserta el agregado que ya produce `EvaluacionOferta.evaluar` y la copia de perfil y oferta del comando
- [X] T019 [US1] Crear `src/main/java/com/gustavo/jobagent/opportunities/application/port/in/ConsultarEvaluacionUseCase.java` y `src/main/java/com/gustavo/jobagent/opportunities/application/service/ConsultarEvaluacionService.java`, devolviendo agregado, copia de perfil y copia de oferta
- [X] T020 [US1] Hacer que `src/main/java/com/gustavo/jobagent/opportunities/application/service/EvaluarOfertaService.java` persista la evidencia en el mismo guardado y no modifique `EvaluacionOferta.evaluar`
- [X] T021 [US1] Registrar el adaptador JPA y el caso de consulta en `src/main/java/com/gustavo/jobagent/configuration/OpportunitiesConfiguration.java`, dejando el repositorio en memoria cuando no haya datasource

**Checkpoint**: US1 se prueba sola, sin registro de ofertas ni de búsquedas.

---

## Phase 4: User Story 2 - Reimportar una oferta sin duplicarla (Priority: P2)

**Goal**: La misma fuente y la misma dirección pública dejan una sola oferta y acumulan procedencias. Otra fuente o otra URL dejan otra oferta.

**Independent Test**: Diez altas de la misma publicación producen una oferta y diez procedencias. Dos portales con el mismo título y la misma empresa producen dos ofertas. No depende del historial de búsquedas.

### Tests for User Story 2

- [X] T022 [P] [US2] Escribir primero, en rojo, `src/test/java/com/gustavo/jobagent/opportunities/domain/IdentidadOfertaTest.java` con la normalización de `research.md`: recortar espacios; rechazar vacío o esquema distinto de `http` y `https`; esquema y host en minúsculas; quitar fragmento; quitar puerto 80 en `http` y 443 en `https`; quitar la barra final del path salvo que el path sea solo `/`; conservar path y query con su capitalización
- [X] T023 [P] [US2] Escribir primero, en rojo, `src/test/java/com/gustavo/jobagent/opportunities/application/service/RegistrarOfertaServiceTest.java` con un doble: diez altas dejan una oferta y diez procedencias; título distinto no crea otra oferta; otra fuente u otra URL sí; sin `urlOriginal` no escribe; el salario ausente no se guarda como 0
- [X] T024 [P] [US2] Escribir primero, en rojo, `src/test/java/com/gustavo/jobagent/opportunities/adapter/out/persistence/OfertaRegistradaPostgresTest.java` para las diez capturas, las dos identidades y dos altas simultáneas de la misma identidad. Cerrar el contexto, abrir otro contra el mismo contenedor y volver a leer: sigue habiendo 1 oferta con 10 procedencias, y las dos ofertas de portales distintos siguen siendo 2

### Implementation for User Story 2

- [X] T025 [P] [US2] Crear el value object `src/main/java/com/gustavo/jobagent/opportunities/domain/IdentidadOferta.java` con `fuenteId` obligatorio y recortado, y `urlNormalizada` calculada con las reglas de T022
- [X] T026 [P] [US2] Crear `src/main/resources/db/migration/V2__oferta_procedencia.sql`. En `oferta`: `titulo` obligatorio de la última captura; `modalidad` en `REMOTE`, `HYBRID`, `ONSITE` o nulo; `fecha_publicacion` nula si se desconoce; los cuatro campos de salario nulos si la última captura no trae salario; prohibido guardar 0 para representar ausencia; `salario_periodo` en `HORA`, `MENSUAL` o `ANUAL`; unicidad de (`fuente_id`, `url_normalizada`). En `procedencia`: `capturado_en` obligatorio y copia de la captura con sus nulos. La reimportación reemplaza la vista vigente con la última captura y no copia hacia adelante un salario viejo
- [X] T027 [P] [US2] Mapear `oferta` en `src/main/java/com/gustavo/jobagent/opportunities/adapter/out/persistence/jpa/OfertaJpaEntity.java`
- [X] T028 [P] [US2] Mapear `procedencia` en `src/main/java/com/gustavo/jobagent/opportunities/adapter/out/persistence/jpa/ProcedenciaJpaEntity.java`, con lectura por `capturado_en` ascendente
- [X] T029 [US2] Declarar `src/main/java/com/gustavo/jobagent/opportunities/application/port/out/OfertaLaboralRepository.java` y los tipos de aplicación `OfertaRegistrada` en `src/main/java/com/gustavo/jobagent/opportunities/application/port/in/OfertaRegistrada.java`
- [X] T030 [US2] Implementar `src/main/java/com/gustavo/jobagent/opportunities/application/service/RegistrarOfertaService.java`: rechaza URL ausente o no normalizable sin escribir; inserta oferta y procedencia en una transacción; ante la restricción única relee y agrega la procedencia
- [X] T031 [US2] Implementar `src/main/java/com/gustavo/jobagent/opportunities/application/service/ConsultarOfertaService.java` para buscar por id interno y por `fuenteId` más `urlOriginal` sin normalizar
- [X] T032 [US2] Implementar `src/main/java/com/gustavo/jobagent/opportunities/adapter/out/persistence/JpaOfertaLaboralRepository.java` traduciendo nulos de salario, fecha y modalidad sin rellenarlos
- [X] T033 [US2] Registrar el caso de uso y el adaptador en `src/main/java/com/gustavo/jobagent/configuration/OpportunitiesConfiguration.java`

**Checkpoint**: US1 y US2 funcionan por separado. Registrar no crea evaluaciones.

---

## Phase 5: User Story 3 - Recordar una búsqueda sin guardar sus ofertas (Priority: P3)

**Goal**: Cada búsqueda queda registrada con sus criterios, fuentes, advertencias y resultado. No inserta ofertas ni evaluaciones. El estado de las fuentes sobrevive al reinicio.

**Independent Test**: Una búsqueda que devuelve ofertas deja en 0 el aumento de ofertas registradas y de evaluaciones. Un registro vacío, uno fallido y uno sin fuentes habilitadas se distinguen al leerlos.

### Tests for User Story 3

- [X] T034 [P] [US3] Escribir primero, en rojo, las aserciones nuevas en `src/test/java/com/gustavo/jobagent/opportunities/application/service/BuscarOfertasServiceTest.java`: el constructor que solo recibe fuentes sigue compilando; `buscar` no registra ofertas ni evaluaciones; los resultados `CON_RESULTADOS`, `RESULTADO_VACIO`, `FALLO_PARCIAL`, `BUSQUEDA_FALLIDA` y `SIN_FUENTES_HABILITADAS` coinciden con `contracts/registro-busqueda.md`; `BUSQUEDA_FALLIDA` y `SIN_FUENTES_HABILITADAS` siguen lanzando `FuentesNoDisponiblesException` después de dejar el registro confirmado
- [X] T035 [P] [US3] Escribir primero, en rojo, `src/test/java/com/gustavo/jobagent/opportunities/adapter/in/web/BusquedaPersistidaPostgresTest.java`: `POST /api/ofertas/busquedas` no aumenta filas de `oferta` ni de `evaluacion` y su cuerpo no gana campos; los tres registros de `quickstart.md` escenario 7 se distinguen en una lectura; tras reiniciar, `SIMULADA` sigue `DISPONIBLE` y habilitada, y `LINKEDIN`, `INDEED` y `GLASSDOOR` siguen `PENDIENTE` y deshabilitadas

### Implementation for User Story 3

- [X] T036 [P] [US3] Crear `src/main/resources/db/migration/V3__busqueda_fuente.sql` con `registro_busqueda` (`resultado` en `CON_RESULTADOS`, `RESULTADO_VACIO`, `FALLO_PARCIAL`, `BUSQUEDA_FALLIDA` o `SIN_FUENTES_HABILITADAS`), `registro_busqueda_fuente`, `registro_busqueda_advertencia` y `fuente_configurada` (`estado` en `DISPONIBLE`, `PENDIENTE` o `DESHABILITADA`). Sembrar `SIMULADA` `DISPONIBLE` habilitada y `LINKEDIN`, `INDEED`, `GLASSDOOR` `PENDIENTE` deshabilitadas. Sin claves foráneas hacia `oferta` ni `evaluacion`
- [X] T037 [P] [US3] Declarar `src/main/java/com/gustavo/jobagent/opportunities/application/port/out/RegistroBusquedaRepository.java` y `src/main/java/com/gustavo/jobagent/opportunities/application/port/out/FuenteConfiguradaRepository.java`
- [X] T038 [US3] Añadir el constructor compatible y la escritura del registro en `src/main/java/com/gustavo/jobagent/opportunities/application/service/BuscarOfertasService.java`, con un registro nulo por defecto y la inserción confirmada antes de propagar `FuentesNoDisponiblesException`
- [X] T039 [US3] Implementar `src/main/java/com/gustavo/jobagent/opportunities/adapter/out/persistence/JpaRegistroBusquedaRepository.java` y `src/main/java/com/gustavo/jobagent/opportunities/adapter/out/persistence/JpaFuenteConfiguradaRepository.java`
- [X] T040 [US3] Implementar `src/main/java/com/gustavo/jobagent/opportunities/application/service/ConsultarRegistroBusquedaService.java` para leer criterios, fuentes consultadas, advertencias, resultado y cantidad sin reconstruir la petición
- [X] T041 [US3] En `src/main/java/com/gustavo/jobagent/configuration/OpportunitiesConfiguration.java`, leer `habilitada` de `SIMULADA` al arrancar y pasarla a `FuenteOfertasSimuladaAdapter`; no crear adaptador para fuentes `PENDIENTE` o con `habilitada` en falso

**Checkpoint**: Las tres historias funcionan. Buscar no llama a registrar ni a evaluar.

---

## Phase 6: Polish & Cross-Cutting Concerns

**Purpose**: Cerrar la memoria del proyecto y la frontera del dominio.

- [X] T042 [P] Actualizar `docs/casos-de-uso/UC-001-buscar-ofertas.md`, `docs/casos-de-uso/UC-002-evaluar-oferta.md`, `docs/README.md` y `PLAN.md` en el mismo cambio que el código: UC-001 deja registro de la búsqueda y sigue sin registrar ofertas ni evaluar; UC-002 recupera la evaluación y su evidencia después de reiniciar. No dar por hechos los portales reales ni el LLM
- [X] T043 [P] Agregar `src/test/java/com/gustavo/jobagent/opportunities/ArchitectureBoundariesTest.java` comprobando que `opportunities.domain` no importa Spring, `jakarta.persistence` ni Hibernate
- [X] T044 Ejecutar `./mvnw test` desde la raíz del repositorio y dejar en verde los escenarios de `specs/001-persist-opportunities/quickstart.md`

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: sin dependencias
- **Foundational (Phase 2)**: depende del setup y bloquea todas las historias
- **User Stories (Phase 3+)**: dependen de la fundación. US2 no necesita US1 para registrarse. US3 no necesita US1 ni US2 para recordar una búsqueda
- **Polish (Phase 6)**: depende de las historias que se quieran dar por cerradas

### User Story Dependencies

- **User Story 1 (P1)**: empieza tras la fundación. No registra ofertas
- **User Story 2 (P2)**: empieza tras la fundación. No crea evaluaciones
- **User Story 3 (P3)**: empieza tras la fundación. No escribe `oferta` ni `evaluacion`. El bean de `OpportunitiesConfiguration.java` se toca en T021, T033 y T041: si las historias avanzan en paralelo, esas tres ediciones se integran al final de cada historia, no a la vez

### Within Each User Story

- Las pruebas de la historia se escriben y fallan antes de su implementación
- La migración y las entidades de una historia pueden ir en paralelo entre sí
- El adaptador espera a sus entidades y al puerto
- La historia queda verificada antes de pasar a la siguiente prioridad

### Parallel Opportunities

- T001, T002 y T003 pueden ir juntos
- T004 y T006 pueden ir juntos; T005 espera a T004
- Tras la fundación, US1, US2 y US3 pueden repartirse entre personas
- Dentro de US1: T007 con T008, y T009–T015 entre sí
- Dentro de US2: T022–T024 entre sí, y T025–T028 entre sí
- Dentro de US3: T034 con T035, y T036 con T037

---

## Parallel Example: User Story 1

```bash
# Pruebas de US1, juntas y en rojo:
Task: "T007 EvaluacionReinicioPostgresTest"
Task: "T008 EvaluarOfertaServiceTest, segunda evaluación"

# Esquema y entidades de US1, juntos:
Task: "T009 V1__evaluacion.sql"
Task: "T010–T015 entidades JPA de evaluación y evidencia"
```

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Cerrar Phase 1 y Phase 2
2. Cerrar Phase 3
3. Parar y validar el reinicio de una evaluación
4. Seguir solo si esa lectura coincide con el agregado y la evidencia guardados

### Incremental Delivery

1. Setup + fundación
2. US1: la evaluación sobrevive. MVP
3. US2: reimportar no duplica
4. US3: la búsqueda queda registrada y no guarda ofertas
5. Phase 6: documentación, frontera de arquitectura y `./mvnw test`

### Parallel Team Strategy

1. Todos cierran setup y fundación
2. Después: una persona por historia, integrando `OpportunitiesConfiguration.java` al terminar cada una
3. Cada historia se demuestra con su prueba independiente

---

## Notes

- `[P]` significa otro archivo y ninguna dependencia pendiente
- No agregar endpoints. Los contratos de esta feature son casos de uso
- `EvaluacionOferta.evaluar` no se modifica
- Un nulo de salario, fecha o modalidad se persiste como nulo, nunca como cero
- Verificar que la prueba nueva falla antes de implementar su historia
