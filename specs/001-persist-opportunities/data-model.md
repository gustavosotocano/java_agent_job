# Data model: Persistencia de oportunidades

Los tipos de dominio ya existentes no cambian su cálculo. Las tablas son del adaptador. Los nombres de columna son el contrato físico; los nombres Java de las entidades JPA se deciden en la implementación y no se filtran al dominio.

## IdentidadOferta (dominio, nuevo value object)

No es una tabla. Es la regla con la que se calcula la clave natural.

| Campo | Regla |
|---|---|
| fuenteId | Obligatorio, recortado. El mismo identificador de `BuscarOfertasPort.fuenteId()`. |
| urlNormalizada | Resultado de normalizar `urlOriginal` según `research.md` sección 4. |

Unicidad lógica: una identidad, una oferta vigente.

## Oferta vigente

Tabla `oferta`.

| Campo | Regla |
|---|---|
| id | Identificador interno, asignado en el primer alta. No forma parte de `OfertaLaboral`. |
| fuente_id | Parte de la identidad. |
| url_normalizada | Parte de la identidad. Única junto con `fuente_id`. |
| id_fuente | Identificador de la publicación dentro de la fuente, tal como viene en la última captura. No es la clave de deduplicación. |
| titulo | Obligatorio. Última captura. |
| empresa | Opcional. |
| descripcion | Opcional. |
| ubicacion | Opcional. |
| modalidad | `REMOTE`, `HYBRID`, `ONSITE` o nulo. Nulo significa desconocida. |
| url_original | Dirección recibida en la última captura, sin normalizar. |
| fecha_publicacion | Opcional. Nulo significa desconocida. |
| salario_minimo, salario_maximo, salario_moneda, salario_periodo | Los cuatro nulos si la última captura no trae salario. Prohibido guardar 0 para representar ausencia. `salario_periodo`: `HORA`, `MENSUAL`, `ANUAL`. |
| tecnologias | Conjunto de la última captura. Vacío si no vinieron. No se infieren desde la descripción. |

Relaciones: una oferta tiene una o más filas en `procedencia`. Borrar una oferta no forma parte de este incremento.

Transiciones: no hay estados de negocio. El alta crea la fila y la primera procedencia. La reimportación actualiza las columnas vigentes e inserta otra procedencia. No hay baja.

## Procedencia

Tabla `procedencia`.

| Campo | Regla |
|---|---|
| id | Identificador de la captura. |
| oferta_id | Oferta a la que pertenece. |
| capturado_en | Momento de esta captura. Obligatorio. |
| id_fuente | Valor de esa captura. |
| url_original | Valor de esa captura. |
| titulo, empresa, descripcion, ubicacion, modalidad | Copia de esa captura, nulos permitidos donde el dominio ya los permite. |
| fecha_publicacion | Nula si esa captura no la traía. |
| salario_* | Nulos si esa captura no traía salario. |
| tecnologias | Copia de esa captura. |

Orden de lectura: `capturado_en` ascendente. Diez importaciones de la misma identidad producen diez filas y una sola `oferta`.

## Evaluación

Tabla `evaluacion`. Solo inserciones.

| Campo | Regla |
|---|---|
| id | El id que ya genera `EvaluacionOferta.evaluar`. Único. |
| id_oferta | El valor actual del agregado: `OfertaLaboral.idFuente()`. |
| id_perfil | `PerfilProfesional.id()`. |
| estado | `COMPATIBLE`, `COMPATIBLE_CON_BRECHAS` o `NO_COMPATIBLE`. |
| fecha_evaluacion | La del agregado. |

Tablas hijas, también solo inserción, conservando el orden de las listas:

- `evaluacion_coincidencia`: `aspecto`, `detalle`, `evidencia`
- `evaluacion_brecha`: `aspecto`, `detalle`, `es_bloqueante`
- `evaluacion_dato_pendiente`: `aspecto`, `pregunta`

`aspecto` usa `TECNOLOGIA`, `MODALIDAD`, `UBICACION`, `SALARIO`, `EXPERIENCIA`.

No hay transición a otro estado. Una segunda evaluación del mismo par inserta otra fila `evaluacion` y no actualiza la anterior.

## Copia de evidencia

Tablas `evaluacion_oferta_evidencia` y `evaluacion_perfil_evidencia`, una fila por evaluación.

La copia de la oferta guarda `fuente_id`, `url_normalizada`, `url_original`, `id_fuente` y el resto de campos de `OfertaLaboral` usados en esa evaluación, salario nulo incluido.

La copia del perfil guarda `id`, `nombre`, `titulo_principal`, tecnologías, modalidades aceptadas, `ubicacion`, expectativa salarial nula si no había, y `anos_experiencia`.

Estas filas no se actualizan cuando la oferta vigente cambia.

## Registro de búsqueda

Tabla `registro_busqueda`. No tiene claves foráneas hacia `oferta` ni hacia `evaluacion`.

| Campo | Regla |
|---|---|
| id | Identificador del registro. |
| ejecutado_en | Momento del intento. |
| cargo | Obligatorio. |
| tecnologias | Conjunto, puede ir vacío. |
| ubicacion | Nula si no hubo filtro. |
| modalidad | Nula si no hubo filtro. |
| limite | Entero ya validado por `CriteriosBusqueda` (1 a 100). |
| resultado | `CON_RESULTADOS`, `RESULTADO_VACIO`, `FALLO_PARCIAL`, `BUSQUEDA_FALLIDA` o `SIN_FUENTES_HABILITADAS`. |
| cantidad_ofertas_devueltas | Entero. No implica filas en `oferta`. |

Tablas hijas:

- `registro_busqueda_fuente`: `fuente_id` de cada fuente que respondió
- `registro_busqueda_advertencia`: `fuente_id` y `mensaje` de cada `AdvertenciaFuente`

`RESULTADO_VACIO` exige al menos una fuente consultada, cero advertencias y cantidad 0. `BUSQUEDA_FALLIDA` exige cero fuentes consultadas y al menos una advertencia. `SIN_FUENTES_HABILITADAS` exige cero fuentes consultadas y cero advertencias de fuente.

## Fuente configurada

Tabla `fuente_configurada`.

| Campo | Regla |
|---|---|
| fuente_id | Clave. |
| nombre | Nombre legible. |
| estado | `DISPONIBLE`, `PENDIENTE`, `DESHABILITADA`. `DISPONIBLE` significa que existe adaptador. `PENDIENTE` significa catálogo sin integración. |
| habilitada | Booleano leído al arrancar el adaptador. |

Semilla de la migración:

| fuente_id | estado | habilitada |
|---|---|---|
| SIMULADA | DISPONIBLE | true |
| LINKEDIN | PENDIENTE | false |
| INDEED | PENDIENTE | false |
| GLASSDOOR | PENDIENTE | false |

No hay transición automática de `PENDIENTE` a `DISPONIBLE` en este incremento.

## Relaciones que no se crean

- Una búsqueda no referencia ofertas guardadas.
- Una evaluación no tiene clave foránea obligatoria hacia `oferta`: la oferta evaluada puede no estar registrada. El vínculo estable es la copia de identidad dentro de la evidencia.
- El perfil no tiene tabla maestra propia. Solo existe la copia guardada con cada evaluación.
