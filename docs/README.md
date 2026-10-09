# Memoria del proyecto

Este directorio conserva los casos de uso y decisiones de implementación junto al código. El plan general está en [PLAN.md](../PLAN.md).

## Casos de uso

| Identificador | Caso | Estado |
|---|---|---|
| UC-001 | [Buscar ofertas de empleo](casos-de-uso/UC-001-buscar-ofertas.md) | Implementado. La búsqueda se registra y no guarda ofertas ni evaluaciones |
| UC-002 | [Evaluar oferta de empleo](casos-de-uso/UC-002-evaluar-oferta.md) | Implementado. La evaluación y su evidencia se recuperan después de reiniciar |

## Decisiones arquitectónicas y patrones

- [Patrón de Portales como Skills / CLIs Desacoplados](arquitectura/portal-skills-pattern.md)

## Registro de Entregas (Handoffs)

- [Handoff 1 — Base técnica M0 y especificación UC-001](handsoff/handsoff_1.md)
- [Handoff 2 — Implementación integral de UC-001 y UC-002](handsoff/handsoff_2.md)

Registrar una oferta y consultar la evaluación guardada forman parte de la persistencia de M4. Registrar perfil sigue sin ficha ni implementación. Los portales reales y el LLM no están hechos.

## Cómo mantener esta memoria

- Un archivo Markdown por caso, con identificador estable y nombre descriptivo.
- Registrar objetivo, alcance, entradas, salidas, reglas, flujo, alternativas y criterios de aceptación.
- Actualizar el documento en el mismo cambio que modifica su comportamiento.
- Diferenciar decisiones confirmadas, propuestas y trabajo pendiente.
- Mantener ejemplos ficticios y excluir credenciales y datos personales.
- Usar [la plantilla](casos-de-uso/PLANTILLA.md) para los siguientes casos.
