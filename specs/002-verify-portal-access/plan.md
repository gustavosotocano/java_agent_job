# Implementation Plan: Verificar acceso a portales públicos

**Branch**: `002-verify-portal-access` | **Date**: 2026-10-10 | **Spec**: [spec.md](spec.md)

**Input**: Feature specification from `/specs/002-verify-portal-access/spec.md`

**Note**: This template is filled in by the `/speckit-plan` command; its definition describes the execution workflow.

## Summary

LinkedIn, Indeed y Glassdoor quedan con un dictamen de vía no comprobada. Ninguno recibe adaptador de búsqueda. Siguen `PENDIENTE` y deshabilitados. La fuente simulada sigue siendo la única que la búsqueda consulta. El dictamen se guarda en PostgreSQL y se vuelve a leer después de reiniciar, por un caso de uso, sin endpoint nuevo.

El detalle de cada portal está en [research.md](research.md). La API oficial que existe hoy sirve para que un socio publique avisos o reciba postulaciones, no para que este agente busque ofertas públicas. No hay cuenta, login ni agregador de terceros.

## Technical Context

**Language/Version**: Java 27

**Primary Dependencies**: Las de M4. No se añade un cliente HTTP, un SDK de portal ni un agregador. Spring Boot sigue en 4.0.8 con Web, validación, actuator, JPA, Flyway y el driver de PostgreSQL.

**Storage**: PostgreSQL 17. Una migración nueva siembra `dictamen_acceso`. No se alteran las filas de `fuente_configurada` sembradas en M4. Desarrollo local con el `docker-compose.yml` ya existente. Pruebas con el mismo Testcontainers de M4.

**Testing**: JUnit y Testcontainers, como en M4. La regla del dictamen se prueba sin Spring. El caso de uso, con un doble. La semilla y el reinicio, contra PostgreSQL. La búsqueda HTTP se vuelve a comprobar para afirmar que los tres portales no salen en `fuentesConsultadas`.

**Target Platform**: Proceso local JVM. Un solo candidato en la máquina de desarrollo.

**Project Type**: Servicio HTTP existente con núcleo de dominio. Este incremento no agrega endpoints.

**Performance Goals**: Ningún objetivo de caudal. La comprobación es funcional: 3 dictámenes no comprobados, 0 portales reales consultables, y una búsqueda que no aumenta ofertas registradas ni evaluaciones.

**Constraints**: El dominio no importa Spring, HTTP, JPA ni SDKs de IA. Una vía no comprobada deja nulos la clase de acceso, la cobertura, los límites y la actualidad. No hay login ni postulaciones. `POST /api/ofertas/busquedas` conserva su contrato. No se llama a LinkedIn, Indeed ni Glassdoor en pruebas ni en runtime. Hace falta Docker para `./mvnw test`.

**Scale/Scope**: Tres dictámenes sembrados y las cuatro fuentes que ya existen. Sin transición a vía comprobada. Sin LLM y sin portales reales usables.

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Principio | Antes de investigar | Después del diseño |
|---|---|---|
| I. El dominio no depende de la infraestructura | Pasa, si el dictamen es un valor de dominio y el SQL queda en el adaptador | Pasa. `DictamenAcceso` no tiene anotaciones de persistencia. JPA y Flyway quedan en el adaptador. No hay cliente HTTP. |
| II. Evaluación determinista | Pasa, si no se toca `EvaluacionOferta.evaluar` | Pasa. Este incremento no evalúa ni guarda evaluaciones nuevas por buscar. No hay LLM. |
| III. Lo desconocido sigue desconocido | Pasa, si un dictamen negativo no rellena clase, cobertura, límites ni actualidad | Pasa. Esos cuatro campos quedan nulos. El motivo existe porque la spec lo exige cuando la vía no está comprobada. La búsqueda vacía y el fallo no se mezclan con "portal sin vía". |
| IV. Pruebas de dentro hacia fuera | Pasa | Pasa. Regla de dictamen sin Spring, caso de uso con doble, semilla y reinicio con Testcontainers, y la búsqueda HTTP existente. [quickstart.md](quickstart.md) recoge ese orden. |
| V. El hito no se adelanta | Pasa, si no se implementa un portal cuya vía no esté comprobada | Pasa. M4.1 verifica los tres y no agrega adaptador, dependencia ni canal HTTP. Buscar no registra ni evalúa. |

Ninguna puerta falla. No hay violación que justificar.

## Project Structure

### Documentation (this feature)

```text
specs/002-verify-portal-access/
├── plan.md
├── research.md
├── data-model.md
├── quickstart.md
├── contracts/
│   ├── dictamen-acceso.md
│   └── busqueda-sin-portales-reales.md
└── tasks.md             # lo crea /speckit-tasks, no este comando
```

### Source Code (repository root)

```text
src/main/java/com/gustavo/jobagent/
├── configuration/OpportunitiesConfiguration.java   # no registra adaptador de portal real
└── opportunities/
    ├── domain/DictamenAcceso.java                  # regla de vía comprobada o no
    ├── application/
    │   ├── port/in/ConsultarDictamenAcceso.java
    │   ├── port/out/DictamenAccesoRepository.java
    │   └── service/ConsultarDictamenAccesoService.java
    └── adapter/out/persistence/                    # JPA del dictamen; sin cliente de portal
src/main/resources/db/migration/V4__dictamen_acceso.sql
src/test/java/com/gustavo/jobagent/                 # dominio, caso de uso, reinicio, búsqueda
```

**Structure Decision**: Se mantiene el módulo `opportunities`. El dictamen es otro puerto de salida, no un cuarto estilo de capas. `BuscarOfertasPort` no gana una implementación por portal. La búsqueda sigue armándose con las fuentes habilitadas que ya resuelve M4, hoy solo `SIMULADA`.

## Complexity Tracking

Ninguna violación de la constitución requiere excepción.
