# Contract: Registro de búsqueda

Canal: efecto interno de `BuscarOfertasUseCase`, más un caso de uso de lectura. No se agregan campos a la respuesta HTTP.

## Escritura

Cada llamada a `buscar` inserta un `registro_busqueda` con los criterios recibidos, las fuentes que respondieron, las advertencias y un `resultado`:

| resultado | Cuándo |
|---|---|
| CON_RESULTADOS | Hay ofertas devueltas |
| RESULTADO_VACIO | Alguna fuente respondió, no hay ofertas y no hay advertencias |
| FALLO_PARCIAL | Alguna fuente respondió y hay al menos una advertencia |
| BUSQUEDA_FALLIDA | Ninguna fuente respondió y hay advertencias. Después se lanza `FuentesNoDisponiblesException`, como hoy |
| SIN_FUENTES_HABILITADAS | No hay fuentes habilitadas. Cero advertencias de fuente. Después se lanza `FuentesNoDisponiblesException`, como hoy |

La inserción queda confirmada aunque el método termine en esa excepción.

## Prohibido en esta escritura

- insertar en `oferta` o `procedencia`
- insertar en `evaluacion`
- invocar `RegistrarOfertaUseCase` o `EvaluarOfertaUseCase`

## Lectura

`ConsultarRegistroBusquedaUseCase` devuelve el registro por id, con criterios, fuentes consultadas, advertencias, resultado y cantidad de ofertas devueltas. Con eso se distingue un vacío de un fallo sin reconstruir la petición.

## HTTP existente

`POST /api/ofertas/busquedas` no cambia su cuerpo: ofertas devueltas, fuentes consultadas y advertencias. El código de error cuando no hay fuentes utilizables tampoco cambia. El efecto nuevo es solo el registro persistido.
