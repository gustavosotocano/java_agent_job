# Data model: Verificar acceso a portales públicos

El dictamen es un hecho de este incremento. No modifica `oferta`, `evaluacion` ni `registro_busqueda`. No cambia las filas ya sembradas de `fuente_configurada`.

## DictamenAcceso

Una fila por portal candidato. Identidad: `fuente_id`.

| Campo | Obligatorio | Regla |
|---|---|---|
| `fuente_id` | sí | Solo `LINKEDIN`, `INDEED` o `GLASSDOOR` |
| `via_comprobada` | sí | En este incremento, las tres filas quedan en falso |
| `motivo` | sí cuando `via_comprobada` es falso | Explica por qué no hay vía pública permitida. Si la única vía exige cuenta o login, el texto lo dice |
| `clase_acceso` | solo si `via_comprobada` es verdadero | `LISTADO_PUBLICO`, `FEED`, `PROVEEDOR_AUTORIZADO` o `BUSQUEDA_PUBLICA_INDEXADA`. Si la vía no está comprobada, el campo queda nulo |
| `cobertura` | solo si `via_comprobada` es verdadero | Texto. Nulo si la vía no está comprobada |
| `limites` | solo si `via_comprobada` es verdadero | Texto. Nulo si la vía no está comprobada |
| `actualidad` | solo si `via_comprobada` es verdadero | Texto. Nulo si la vía no está comprobada |
| `verificado_en` | sí | Momento de la verificación, `2026-10-10`, con zona horaria |

No se usa `0`, cadena vacía ni un literal de relleno para un campo que la vía no comprobada deja desconocido.

### Semilla

| fuente_id | via_comprobada | motivo (resumen) | clase, cobertura, límites, actualidad |
|---|---|---|---|
| `LINKEDIN` | falso | La API oficial publicada es de socios para publicar avisos y postulaciones, no para buscar. No hay acuerdo ni una API pública de búsqueda | nulos |
| `INDEED` | falso | La búsqueda pública de publishers está retirada. Las APIs actuales de socios envían avisos y postulaciones hacia Indeed y exigen alta | nulos |
| `GLASSDOOR` | falso | La API de socios que exponía empleos ya no atiende y no acepta altas nuevas. La vía empresarial reparte avisos desde un ATS, no permite buscar | nulos |

El texto completo del motivo vive en la migración. La tabla de arriba es el contenido que la lectura tiene que conservar.

## Fuente configurada

Sin cambio de estado en este incremento.

| fuente_id | estado | habilitada | ¿Se consulta? |
|---|---|---|---|
| `SIMULADA` | `DISPONIBLE` | sí | sí |
| `LINKEDIN` | `PENDIENTE` | no | no |
| `INDEED` | `PENDIENTE` | no | no |
| `GLASSDOOR` | `PENDIENTE` | no | no |

Un dictamen con `via_comprobada` en falso no puede convivir con `estado = DISPONIBLE` ni con `habilitada = sí` para ese mismo `fuente_id`. Este incremento no escribe esa combinación.

## Relaciones

- `DictamenAcceso.fuente_id` nombra una `FuenteConfigurada` de portal real. No hay fila de dictamen para `SIMULADA`.
- La búsqueda no lee el dictamen para decidir el HTTP. La decisión de no consultar el portal ya está en `habilitada` y `estado`. El dictamen explica esa decisión y se lee por su propio caso de uso.
- Buscar no crea ofertas ni evaluaciones.

## Transiciones

Este incremento no cambia un dictamen después de sembrarlo. Pasar un portal a vía comprobada, habilitarlo y consultarlo queda fuera de M4.1: exigiría una verificación nueva y un adaptador que hoy no existe.

## Validación de dominio

`DictamenAcceso` rechaza:

- `fuente_id` vacío o distinto de los tres portales
- vía no comprobada sin motivo, o con motivo en blanco
- vía no comprobada con clase, cobertura, límites o actualidad informados
- vía comprobada sin alguna de esas cuatro descripciones
- un motivo que declare cuenta personal o inicio de sesión junto con vía comprobada
