# Contract: Registrar oferta

Canal: caso de uso de aplicación `RegistrarOfertaUseCase`. No hay endpoint HTTP en este incremento.

## Entrada

Una `OfertaLaboral` ya válida según el dominio.

Precondición adicional de este caso de uso: `urlOriginal` presente y normalizable. Si falta o no se puede normalizar, el caso de uso falla y no escribe filas.

## Salida

`OfertaRegistrada`:

- `id` interno estable
- identidad (`fuenteId`, `urlNormalizada`)
- campos vigentes, iguales a la última captura
- procedencias en orden de captura, cada una con su `capturadoEn` y la copia de lo recibido, nulos incluidos

## Efectos

| Situación | Ofertas vigentes | Procedencias |
|---|---|---|
| Primera vez para esa identidad | 1 nueva | 1 |
| Misma identidad, incluso si cambió el título o el `idFuente` | la misma fila, campos vigentes reemplazados por la captura nueva | +1 |
| Otra fuente, o la misma fuente con otra URL normalizada | otra fila, aunque coincidan empresa y título | 1 en la oferta nueva |
| Dos altas simultáneas de la misma identidad | 1 | 2 |

## Fuera de este contrato

No evalúa. No escribe `registro_busqueda`. No llama a un portal.
