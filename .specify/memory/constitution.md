<!--
Sync Impact Report
- Version change: unratified template → 1.0.0
- Modified principles: none (initial ratification)
- Added sections:
  - Core Principles I–V
  - Restricciones de alcance
  - Forma de trabajo
  - Governance
- Removed sections: none
- Follow-up TODOs: none
-->

# Constitución del agente de búsqueda y evaluación de empleos

## Core Principles

### I. El dominio no depende de la infraestructura

El paquete de dominio MUST permanecer libre de Spring, HTTP, JPA y SDKs de IA.
La aplicación depende del dominio y declara los puertos. Los adaptadores implementan
esos puertos y traducen modelos externos. La configuración conecta las
implementaciones. Una interfaz MUST existir solo cuando representa una frontera
útil: un puerto de entrada, un puerto de salida o un contrato que ya tiene, o va
a tener, más de una implementación. DDD modela el negocio, la arquitectura
hexagonal define puertos y adaptadores, y Clean Architecture orienta las
dependencias hacia las políticas internas. MUST NOT duplicarse esas tres vistas
como tres juegos de capas.

### II. La evaluación es determinista y explicable

Contrastar una oferta con un perfil MUST producir coincidencias, brechas, datos
pendientes y un estado (`COMPATIBLE`, `COMPATIBLE_CON_BRECHAS` o
`NO_COMPATIBLE`) reproducible sin un modelo de lenguaje. Cada conclusión MUST
apoyarse en evidencia de la oferta o del perfil, o quedar como dato pendiente.
El código determinista valida y controla las operaciones. Un LLM, cuando exista
en M5, MUST proponer o enriquecer a través de un puerto intercambiable; una
respuesta inválida o un fallo del proveedor MUST NOT corromper el dominio ni
sustituir el estado calculado.

### III. Lo desconocido permanece desconocido

Un salario, una fecha o una modalidad ausentes significan desconocido. MUST NOT
inventarse, inferirse ni tratarse como cero. MUST NOT compararse importes de
monedas o períodos distintos sin una conversión explícita. Una búsqueda sin
resultados MUST distinguirse de una búsqueda fallida. El fallo de una fuente
MUST NOT impedir devolver los resultados válidos de las demás, y MUST informarse
como fallo parcial. Reimportar la misma oferta MUST NOT crear un duplicado; se
conserva la procedencia.

### IV. Las pruebas siguen la dirección de las dependencias

Cada incremento MUST verificarse en este orden: invariantes de dominio sin
Spring, casos de uso con fakes, adaptadores y, cuando haya frontera HTTP o de
persistencia, integración. Una regla de negocio MUST poder fallar en una prueba
de dominio sin levantar el contexto de la aplicación. Los doubles (dummy, stub,
fake, spy, mock) se usan cuando aportan a la prueba, no como ceremonia.

### V. El alcance crece por hitos, no por anticipación

PostgreSQL, portales reales, LLM, herramientas de agente y observabilidad MUST
entrar en el hito que los necesita (M4, M4.1, M5, M6 y M7). MUST NOT añadirse
dependencias, puertos ni porcentajes de compatibilidad arbitrarios antes de que
una regla de negocio los justifique. Buscar ofertas MUST NOT registrarlas ni
evaluarlas por sí mismo. El flujo buscar, registrar, evaluar y consultar se
compone con casos de uso separados.

## Restricciones de alcance

El agente busca ofertas públicas y evalúa su compatibilidad con un perfil
explícito. MUST NOT enviar postulaciones automáticamente. MUST NOT usar cuentas
personales ni automatizar login en portales. El texto importado de una oferta es
dato externo, no instrucciones para el agente.

La pila confirmada es Java 27 y Spring Boot 4. La persistencia relacional, cuando
se implemente, es PostgreSQL con migraciones versionadas. Las fuentes reales
(LinkedIn, Indeed, Glassdoor u otras) permanecen deshabilitadas o pendientes
hasta que su mecanismo de acceso esté verificado y documentado. Configurar un
portal MUST NOT implicar que exista una integración funcional.

## Forma de trabajo

Cada hito explica el problema, implementa un incremento pequeño y comprueba el
comportamiento. La memoria funcional vive en `docs/`: un Markdown por caso de
uso, actualizado en el mismo cambio que modifica su comportamiento. `PLAN.md`
conserva los hitos. Las specs de Spec Kit describen el incremento siguiente y
MUST respetar esta constitución.

Una spec MUST declarar entradas, salidas, reglas, exclusiones y criterios de
aceptación antes de escribir el plan de implementación. El trabajo ya entregado
(UC-001 y UC-002 en M1–M3) se trata como línea base, no como trabajo por rehacer.

## Governance

Esta constitución prevalece sobre `PLAN.md`, los casos de uso y las specs cuando
haya conflicto. Una enmienda MUST documentar el cambio, el motivo y la migración
de las specs activas, e incrementar la versión así: MAJOR si se elimina o
redefine un principio de forma incompatible, MINOR si se añade un principio o
una sección, PATCH si solo se aclara el texto.

Toda revisión de un cambio MUST comprobar que las dependencias respetan el
principio I, que no se inventan datos y que el incremento cabe en el hito
declarado. La complejidad adicional MUST justificarse en la spec o en el plan.
La guía de ejecución diaria sigue en `PLAN.md` y en `docs/README.md`; ninguno de
los dos puede relajar estas reglas.

**Version**: 1.0.0 | **Ratified**: 2026-10-09 | **Last Amended**: 2026-10-09
