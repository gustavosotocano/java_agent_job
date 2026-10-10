# Contrato: ejecución del agente

No es un endpoint. Quien ya puede buscar, consultar y evaluar pide una ejecución y recibe el cierre.

## Entrada

Un perfil explícito y una lista ordenada de pasos. Cada paso tiene un nombre y, si aplica, criterios de búsqueda o un identificador. Ningún paso incluye un estado de compatibilidad.

## Salida

- Cierre: `COMPLETADA`, `LIMITE_ALCANZADO` o `HERRAMIENTA_NO_AUTORIZADA`.
- Pasos realizados, en orden, con nombre, si estaba autorizado y un resumen.
- Nombres pedidos que no se ejecutaron.

## Herramientas

| Nombre | Efecto |
|---|---|
| buscarOfertas | Devuelve el resultado de la búsqueda que ya existe. No registra ofertas ni crea evaluaciones. |
| consultarPerfil | Devuelve el perfil de esta ejecución si el identificador coincide. Si no, desconocido. |
| consultarOferta | Devuelve la oferta registrada con ese identificador, o desconocida. |
| guardarEvaluacion | Si la oferta está registrada, guarda la evaluación que calcula el contraste con el perfil de la ejecución. Si no está registrada, no guarda nada. |

## Fuera de este contrato

No hay postulación, login, registro de perfil, registro de oferta como herramienta, ni llamada a un proveedor para elegir el paso. Los portales reales no cambian.
