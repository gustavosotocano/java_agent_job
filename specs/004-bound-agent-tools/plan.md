# Implementation Plan: Ejecución acotada del agente

**Branch**: `004-bound-agent-tools` | **Date**: 2026-10-10 | **Spec**: [spec.md](spec.md)

**Input**: Feature specification from `/specs/004-bound-agent-tools/spec.md`

**Note**: This template is filled in by the `/speckit-plan` command; its definition describes the execution workflow.

## Summary

Una ejecución recibe una lista de pasos y un perfil. El dominio autoriza solo `buscarOfertas`, `consultarPerfil`, `consultarOferta` y `guardarEvaluacion`, corta en el primer nombre distinto y no deja pasar de 4. El caso de uso llama a los casos de uso que ya existen. `guardarEvaluacion` no acepta un estado: usa `EvaluacionOferta.evaluar` a través del caso de uso actual. No hay proveedor, ni tabla nueva, ni endpoint.

El detalle está en [research.md](research.md) y [data-model.md](data-model.md).

## Technical Context

**Language/Version**: Java 27

**Primary Dependencies**: Las de M5. No se añade un SDK de agente ni un cliente HTTP. Spring Boot sigue en 4.0.8.

**Storage**: El de M4 y M5. Esta ejecución no crea tablas. Las evaluaciones que guarda `guardarEvaluacion` siguen en `evaluacion` y sus hijas.

**Testing**: JUnit. La partición de la lista se prueba sin Spring. El caso de uso, con dobles de buscar, consultar oferta y evaluar. No hace falta un proveedor.

**Target Platform**: Proceso local JVM. Un solo candidato en la máquina de desarrollo.

**Project Type**: Servicio con núcleo de dominio. Este incremento no agrega endpoints.

**Performance Goals**: Ningún objetivo de caudal. El tope es 4 pasos por ejecución.

**Constraints**: El dominio no importa Spring, HTTP, JPA ni SDKs de IA. Buscar no registra ni evalúa. No hay login ni postulaciones. No se llama al puerto de propuesta para elegir el paso. Hace falta Docker para `./mvnw test` por las pruebas que ya lo usan.

**Scale/Scope**: Un caso de uso y cuatro nombres de herramienta. Sin historial de ejecuciones y sin métricas.

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Principio | Antes de investigar | Después del diseño |
|---|---|---|
| I. El dominio no depende de la infraestructura | Pasa, si la lista se parte en el dominio y las llamadas quedan en la aplicación | Pasa. `EjecucionAcotada` no tiene Spring ni SDK. Los casos de uso existentes siguen detrás de sus puertos. |
| II. Evaluación determinista | Pasa, si guardar no acepta un estado | Pasa. El paso solo trae un identificador de oferta. El estado sale de `evaluar`. |
| III. Lo desconocido sigue desconocido | Pasa, si un id que no existe no fabrica perfil ni oferta | Pasa. Perfil u oferta desconocidos quedan como paso no resuelto. Un salario ausente no entra en este hito. |
| IV. Pruebas de dentro hacia fuera | Pasa | Pasa. Regla de dominio sin Spring y caso de uso con dobles. [quickstart.md](quickstart.md) recoge ese orden. |
| V. El hito no se adelanta | Pasa, si no hay métricas, proveedor ni canal nuevo | Pasa. M6 acota las cuatro herramientas. M7 no se adelanta. Buscar no registra ni evalúa. |

Ninguna puerta falla. No hay violación que justificar.

## Project Structure

### Documentation (this feature)

```text
specs/004-bound-agent-tools/
├── plan.md
├── research.md
├── data-model.md
├── quickstart.md
├── contracts/
│   └── ejecucion-agente.md
└── tasks.md
```

### Source Code (repository root)

```text
src/main/java/com/gustavo/jobagent/
├── configuration/OpportunitiesConfiguration.java
└── opportunities/
    ├── domain/EjecucionAcotada.java
    ├── application/
    │   ├── port/in/EjecutarAgenteUseCase.java
    │   └── service/EjecutarAgenteService.java
src/test/java/com/gustavo/jobagent/
docs/casos-de-uso/UC-003-ejecutar-agente.md
```

**Structure Decision**: Se mantiene el módulo `opportunities`. La ejecución es otro caso de uso de entrada. No envuelve a los cuatro en un framework de agente ni añade un controlador.

## Complexity Tracking

Ninguna violación de la constitución requiere excepción.
