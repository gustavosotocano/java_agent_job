# Implementation Plan: Persistencia de oportunidades

**Branch**: `001-persist-opportunities` | **Date**: 2026-10-09 | **Spec**: [spec.md](spec.md)

**Input**: Feature specification from `/specs/001-persist-opportunities/spec.md`

**Note**: This template is filled in by the `/speckit-plan` command; its definition describes the execution workflow.

## Summary

Las ofertas registradas, las evaluaciones completadas, el catálogo de fuentes y el registro de cada búsqueda quedan en PostgreSQL y siguen disponibles después de reiniciar. Reimportar la misma publicación de la misma fuente actualiza una sola oferta y acumula procedencias. Buscar escribe ese registro y no registra ofertas ni evaluaciones.

El enfoque técnico está en [research.md](research.md): Flyway y JPA solo en adaptadores, la normalización de la dirección pública en el dominio, copias inmutables de la evidencia de cada evaluación, y el mismo contrato HTTP de UC-001. No se abre un canal REST nuevo.

## Technical Context

**Language/Version**: Java 27

**Primary Dependencies**: Spring Boot 4.0.8 (`spring-boot-starter-webmvc`, validation, actuator). Se añaden `spring-boot-starter-data-jpa`, `spring-boot-starter-flyway`, el módulo PostgreSQL de Flyway si no viene transitivo, y el driver `org.postgresql:postgresql`.

**Storage**: PostgreSQL 17. Esquema en `src/main/resources/db/migration`. Desarrollo local con Docker Compose. Pruebas con Testcontainers y `@ServiceConnection`.

**Testing**: JUnit vía `spring-boot-starter-test`, más `spring-boot-testcontainers`, `testcontainers-junit-jupiter` y `testcontainers-postgresql`. Dominio y casos de uso siguen sin contenedor; el adaptador y el arranque de contexto, no.

**Target Platform**: Proceso local JVM. El candidato es un solo usuario en la máquina de desarrollo.

**Project Type**: Servicio HTTP existente con núcleo de dominio. Este incremento no agrega interfaz ni endpoints.

**Performance Goals**: Ningún objetivo de caudal. La comprobación es funcional: 10 importaciones dejan 1 oferta, una búsqueda deja en 0 el aumento de ofertas y de evaluaciones, y el 100 % de las evaluaciones se releen igual tras reiniciar el contexto contra la misma base.

**Constraints**: El dominio no importa Spring, HTTP, JPA ni SDKs de IA. Un salario, una fecha o una modalidad ausentes se guardan y se leen como nulos. No hay login de portales ni postulaciones. `POST /api/ofertas/busquedas` conserva su respuesta y sus errores. Hace falta Docker para `./mvnw test`.

**Scale/Scope**: Catálogo simulado actual, cuatro fuentes sembradas y el volumen de un uso local. Sin retención ni borrado. Sin portales reales ni LLM.

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Principio | Antes de investigar | Después del diseño |
|---|---|---|
| I. El dominio no depende de la infraestructura | Pasa, si JPA queda en el adaptador | Pasa. La única regla nueva de dominio es la identidad de la dirección pública, sin anotaciones de persistencia. Las tablas están en [data-model.md](data-model.md) como contrato del adaptador. |
| II. Evaluación determinista | Pasa, si no se toca `EvaluacionOferta.evaluar` | Pasa. El guardado inserta el agregado tal cual y una copia de la evidencia. No hay LLM. |
| III. Lo desconocido sigue desconocido | Pasa, si el esquema admite nulos y no usa cero | Pasa. Salario, fecha y modalidad nulos siguen nulos en la vista vigente y en cada procedencia. La búsqueda vacía, el fallo y la ausencia de fuentes son resultados distintos. |
| IV. Pruebas de dentro hacia fuera | Pasa | Pasa. Identidad en dominio, casos de uso con dobles, adaptador y reinicio con Testcontainers. [quickstart.md](quickstart.md) recoge ese orden. |
| V. El hito no se adelanta | Pasa | Pasa. Entra PostgreSQL, que corresponde a M4. No entran portales reales, LLM, agente ni observabilidad. Buscar no registra ni evalúa. No hay canal HTTP nuevo. |

Ninguna puerta falla. No hay violación que justificar.

## Project Structure

### Documentation (this feature)

```text
specs/001-persist-opportunities/
├── plan.md
├── research.md
├── data-model.md
├── quickstart.md
├── contracts/
│   ├── registrar-oferta.md
│   ├── consultar-oferta.md
│   ├── evaluacion-almacenada.md
│   ├── registro-busqueda.md
│   └── fuente-configurada.md
└── tasks.md             # lo crea /speckit-tasks, no este comando
```

### Source Code (repository root)

```text
src/main/java/com/gustavo/jobagent/
├── configuration/OpportunitiesConfiguration.java
└── opportunities/
    ├── domain/                         # IdentidadOferta nueva; el resto intacto
    ├── application/
    │   ├── port/in/                    # registrar, consultar oferta, evaluación y búsqueda
    │   ├── port/out/                   # repos de oferta, registro y fuente; evaluación existente
    │   └── service/
    └── adapter/
        ├── in/web/                     # sin controladores nuevos
        └── out/persistence/            # JPA + el repositorio en memoria para pruebas
src/main/resources/
├── application.yml                     # datasource por variables de entorno
└── db/migration/                       # Flyway
src/test/java/com/gustavo/jobagent/     # dominio, casos de uso, adaptador, reinicio
docker-compose.yml                      # solo PostgreSQL local
```

**Structure Decision**: Se mantiene el módulo único `opportunities` ya separado en dominio, aplicación y adaptadores. La persistencia es otro adaptador de salida detrás de los puertos, no un proyecto nuevo ni una capa paralela. El repositorio en memoria de evaluaciones permanece para las pruebas que no necesitan PostgreSQL. El bean de producción lo sustituye cuando hay datasource, aprovechando el `@ConditionalOnMissingBean` ya existente y el mismo criterio para los puertos nuevos.

## Complexity Tracking

Ninguna violación de la constitución requiere excepción.
