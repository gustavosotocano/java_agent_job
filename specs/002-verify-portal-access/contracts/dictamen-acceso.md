# Contract: Dictamen de acceso

Canal: caso de uso de lectura `ConsultarDictamenAcceso`. No se agrega un endpoint HTTP.

## Lectura

`porFuente(fuenteId)` devuelve un dictamen o vacío si el identificador no es uno de los tres portales sembrados.

`listar()` devuelve los tres dictámenes, en orden estable: `LINKEDIN`, `INDEED`, `GLASSDOOR`.

Cada dictamen expone:

| Campo | En este incremento |
|---|---|
| `fuenteId` | `LINKEDIN`, `INDEED` o `GLASSDOOR` |
| `viaComprobada` | `false` |
| `motivo` | texto no vacío, el de la semilla |
| `claseAcceso` | nulo |
| `cobertura` | nulo |
| `limites` | nulo |
| `actualidad` | nulo |
| `verificadoEn` | `2026-10-10` con zona horaria |

`SIMULADA` no tiene dictamen. Pedirla devuelve vacío, no un error de disponibilidad.

## Prohibido en esta lectura

- consultar LinkedIn, Indeed o Glassdoor
- cambiar `fuente_configurada`
- registrar ofertas o crear evaluaciones
- iniciar sesión o postular

## Duración

Cerrar el proceso y abrirlo de nuevo contra la misma base devuelve los mismos tres dictámenes. La semilla no se reescribe en cada arranque.
