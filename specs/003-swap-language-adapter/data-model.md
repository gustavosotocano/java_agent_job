# Data model: Adaptador de lenguaje intercambiable

## ObservacionPropuesta

Valor inmutable. No tiene identidad propia.

| Campo | Regla |
|---|---|
| texto | Obligatorio. Se recorta. Rechaza nulo o blanco. |
| fragmento | Obligatorio. Se recorta. Rechaza nulo o blanco. Tiene que aparecer, sin distinguir mayúsculas, en el corpus de la oferta y del perfil para que la observación se acepte. |

Una observación candidata se descarta entera si el texto afirma un número, una modalidad, un estado de evaluación o un token técnico que el corpus no contiene. No se corrige el texto.

## ResultadoPropuesta

Resultado del puerto, antes de filtrar contra el corpus.

| Variante | Contenido |
|---|---|
| Disponible | Lista de observaciones candidatas. Puede estar vacía. |
| NoDisponible | Motivo `FALLO`, `TIEMPO_AGOTADO` o `ILEGIBLE`. Equivale a cero observaciones guardadas. |

`ILEGIBLE` también aplica cuando el texto estructurado no trae bloques `texto` y `fragmento` completos. No se conserva un bloque a medias.

## EvaluacionOferta

El agregado gana una lista `observaciones`. `evaluar` la deja vacía. `conObservaciones` sustituye solo esa lista y conserva `estado`, `coincidencias`, `brechas`, `datosPendientes`, identificadores y fecha.

No hay transición de estado nueva. La propuesta vacía no es un error.

## Corpus

Texto sobre el que se busca el fragmento y las afirmaciones. Incluye, cuando existen, título, empresa, descripción, ubicación, modalidad, salario y tecnologías de la oferta, y nombre, título, tecnologías, modalidades, ubicación, expectativa salarial y años de experiencia del perfil. Un salario ausente no aporta `0`.

## evaluacion_observacion

| Columna | Regla |
|---|---|
| evaluacion_id | Obligatorio. Referencia a `evaluacion.id`. |
| orden | Obligatorio. Junto con `evaluacion_id` forma la clave. Empieza en 0 y sigue el orden aceptado. |
| texto | Obligatorio. `CHECK` de longitud recortada mayor que 0. |
| fragmento | Obligatorio. `CHECK` de longitud recortada mayor que 0. |

Solo inserción junto con la evaluación. Cero filas significa propuesta vacía. No hay `UPDATE` de observaciones de una evaluación ya guardada.

## Adaptador

No es una entidad persistida. El predeterminado se llama `vacia`. El alternativo se llama `texto` y solo lee la propiedad local `jobagent.lenguaje.texto`.
