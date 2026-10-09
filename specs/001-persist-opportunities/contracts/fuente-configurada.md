# Contract: Fuente configurada

Canal: repositorio de aplicación leído al arrancar. No hay endpoint para modificar fuentes en este incremento.

## Lectura

Cada fila expone `fuenteId`, `nombre`, `estado` (`DISPONIBLE`, `PENDIENTE`, `DESHABILITADA`) y `habilitada`.

## Uso en el arranque

La configuración de Spring lee `SIMULADA` y pasa `habilitada` al `FuenteOfertasSimuladaAdapter` que ya existe. Una fuente `PENDIENTE` o con `habilitada` en falso no recibe adaptador de búsqueda y no se consulta.

## Semilla

La migración inserta `SIMULADA` disponible y habilitada, y `LINKEDIN`, `INDEED` y `GLASSDOOR` pendientes y deshabilitadas.

Reiniciar contra la misma base devuelve los mismos cuatro estados. Cambiar el booleano de `SIMULADA` en la base y reiniciar cambia si esa fuente se consulta. Este incremento no ofrece un comando para hacer ese cambio; la prueba de integración puede actualizar la fila por el repositorio.
