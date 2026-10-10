# Data model: Ejecución acotada del agente

## SolicitudPaso

Valor de entrada. No se persiste.

| Campo | Regla |
|---|---|
| herramienta | Obligatorio. Solo `buscarOfertas`, `consultarPerfil`, `consultarOferta` y `guardarEvaluacion` se ejecutan. Cualquier otro texto, incluidas variaciones de mayúsculas, es no autorizado. |
| criterios | Solo para `buscarOfertas`. Si faltan, el paso corre y queda como no resuelto, sin buscar. |
| id | Para `consultarPerfil`, `consultarOferta` y `guardarEvaluacion`. Si falta o no coincide con un dato existente, el paso queda no resuelto. No trae un estado de evaluación. |

## PlanEjecucion

Lo decide el dominio antes de llamar a una herramienta.

| Campo | Regla |
|---|---|
| estado | `COMPLETADA`, `LIMITE_ALCANZADO` o `HERRAMIENTA_NO_AUTORIZADA`. |
| aEjecutar | Como máximo 4 solicitudes autorizadas, anteriores a un nombre prohibido. |
| rechazada | La solicitud no autorizada que detuvo la lista, o ninguna. |
| noEjecutadas | Nombres pedidos que no llegaron a ejecutarse, sin contar la rechazada, que ya va en el resultado como no autorizada. |

Una lista vacía produce `COMPLETADA`, cero a ejecutar y cero no ejecutadas.

## PasoRealizado

| Campo | Regla |
|---|---|
| herramienta | El nombre pedido. |
| autorizada | Falso solo en el paso que detuvo la ejecución. |
| resumen | Qué ocurrió: cuántas ofertas devolvió la búsqueda, perfil encontrado o desconocido, oferta encontrada o desconocida, evaluación guardada con su estado, oferta no registrada, criterios ausentes, o no autorizada. |

## EjecucionRealizada

Cierre, pasos realizados y nombres no ejecutados. No tiene tabla.

## Lo que no se añade

No hay entidad de perfil persistida. No hay fila de ejecución. `EvaluacionOferta` no gana campos.
