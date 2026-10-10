# Contrato: evaluación consultada

Quien consulta una evaluación por su identificador recibe el mismo agregado de antes, más la sección de propuesta. No se añade un canal HTTP.

## Sección de propuesta

Lista `observaciones`. Cada elemento tiene `texto` y `fragmento`. La lista vacía es un resultado válido: el proveedor no aportó nada, falló, agotó el tiempo, devolvió algo ilegible o todas las candidatas fueron rechazadas.

La sección no forma parte de `coincidencias`, `brechas` ni `datosPendientes`. No modifica `estado`.

## Lo que no cambia

- El estado sigue siendo `COMPATIBLE`, `COMPATIBLE_CON_BRECHAS` o `NO_COMPATIBLE`, calculado solo con los datos presentes.
- Un salario, una modalidad o una tecnología ausentes siguen ausentes.
- Una segunda evaluación del mismo par tiene otro identificador. La primera conserva su estado y su número de observaciones.
- Después de reiniciar, el identificador devuelve el mismo estado y la misma cantidad de observaciones.
