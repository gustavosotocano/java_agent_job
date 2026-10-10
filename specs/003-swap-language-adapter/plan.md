# Implementation Plan: Adaptador de lenguaje intercambiable

**Branch**: `003-swap-language-adapter` | **Date**: 2026-10-10 | **Spec**: [spec.md](spec.md)

**Input**: Feature specification from `/specs/003-swap-language-adapter/spec.md`

**Note**: This template is filled in by the `/speckit-plan` command; its definition describes the execution workflow.

## Summary

La evaluación sigue calculándose en `EvaluacionOferta.evaluar`. El caso de uso pide una propuesta a un puerto intercambiable y, si hace falta, adjunta solo observaciones ya aceptadas. El adaptador predeterminado responde vacío y no sale del proceso. Otro adaptador lee un texto estructurado local. Un fallo, un tiempo agotado o un texto ilegible dejan la evaluación guardada con cero observaciones. No hay SDK, ni proveedor comercial, ni endpoint nuevo.

Las reglas de aceptación están en [research.md](research.md). El agregado y la tabla nueva están en [data-model.md](data-model.md).

## Technical Context

**Language/Version**: Java 27

**Primary Dependencies**: Las de M4. No se añade un SDK de modelo de lenguaje ni un cliente HTTP. Spring Boot sigue en 4.0.8 con Web, validación, actuator, JPA, Flyway y el driver de PostgreSQL.

**Storage**: PostgreSQL 17. La migración `V5` crea `evaluacion_observacion` como hija de solo inserción. Cero filas significan propuesta vacía. Desarrollo local con el `docker-compose.yml` ya existente. Pruebas con el mismo Testcontainers de M4.

**Testing**: JUnit y Testcontainers, como en M4. La aceptación de observaciones se prueba sin Spring. El caso de uso, con un doble del puerto. El intercambio, con los dos adaptadores. El reinicio, contra PostgreSQL.

**Target Platform**: Proceso local JVM. Un solo candidato en la máquina de desarrollo.

**Project Type**: Servicio con núcleo de dominio. Este incremento no agrega endpoints. La evaluación se consulta por el caso de uso que ya existe.

**Performance Goals**: Ningún objetivo de caudal. El puerto tiene un límite de 2 segundos. El adaptador predeterminado responde al instante. La comprobación es funcional: el estado calculado no cambia y la propuesta inválida no se guarda.

**Constraints**: El dominio no importa Spring, HTTP, JPA ni SDKs de IA. `evaluar` no recibe el puerto. Lo desconocido sigue desconocido: un número, una modalidad, un estado o un token técnico ausente del corpus no se copian a la propuesta ni rellenan la oferta. No hay login ni postulaciones. Buscar no llama al puerto. Hace falta Docker para `./mvnw test`.

**Scale/Scope**: Un puerto, dos adaptadores locales y la lista de observaciones de cada evaluación nueva. Sin agente de herramientas y sin proveedor comercial.

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Principio | Antes de investigar | Después del diseño |
|---|---|---|
| I. El dominio no depende de la infraestructura | Pasa, si el puerto vive en la aplicación y los adaptadores traducen | Pasa. `ObservacionPropuesta` y el filtro no tienen Spring ni SDK. JPA y Flyway quedan en el adaptador. No hay cliente HTTP. |
| II. Evaluación determinista | Pasa, si `evaluar` no cambia y la propuesta no sustituye el estado | Pasa. `evaluar` deja la lista vacía. `conObservaciones` no toca estado, coincidencias, brechas ni datos pendientes. Un fallo del puerto guarda la evaluación igual. |
| III. Lo desconocido sigue desconocido | Pasa, si una afirmación sin respaldo se descarta | Pasa. El filtro rechaza número, modalidad, estado o token técnico que el corpus no contiene. No escribe `0` en un salario ausente. |
| IV. Pruebas de dentro hacia fuera | Pasa | Pasa. Regla de dominio sin Spring, caso de uso con doble, adaptadores y reinicio con Testcontainers. [quickstart.md](quickstart.md) recoge ese orden. |
| V. El hito no se adelanta | Pasa, si no hay SDK, agente ni canal nuevo | Pasa. M5 entrega el puerto y dos adaptadores locales. M6 y M7 no se adelantan. Buscar no registra ni evalúa. |

Ninguna puerta falla. No hay violación que justificar.

## Project Structure

### Documentation (this feature)

```text
specs/003-swap-language-adapter/
├── plan.md
├── research.md
├── data-model.md
├── quickstart.md
├── contracts/
│   ├── propuesta-lenguaje.md
│   └── evaluacion-con-propuesta.md
└── tasks.md             # lo crea /speckit-tasks, no este comando
```

### Source Code (repository root)

```text
src/main/java/com/gustavo/jobagent/
├── configuration/OpportunitiesConfiguration.java
└── opportunities/
    ├── domain/ObservacionPropuesta.java
    ├── domain/Propuesta.java
    ├── domain/ResultadoPropuesta.java
    ├── domain/EvaluacionOferta.java          # lista vacía al evaluar; conObservaciones
    ├── application/
    │   ├── port/out/PropuestaLenguajePort.java
    │   └── service/EvaluarOfertaService.java
    └── adapter/out/lenguaje/
        ├── PropuestaVaciaAdapter.java
        └── PropuestaTextoAdapter.java
src/main/resources/db/migration/V5__evaluacion_observacion.sql
src/test/java/com/gustavo/jobagent/
```

**Structure Decision**: Se mantiene el módulo `opportunities`. El puerto de propuesta es otra frontera de salida, no un segundo estilo de capas. `EvaluacionOferta.evaluar` no recibe el adaptador. No se crea un controlador.

## Complexity Tracking

Ninguna violación de la constitución requiere excepción.
