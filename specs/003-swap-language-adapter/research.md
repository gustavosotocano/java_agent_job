# Research: Adaptador de lenguaje intercambiable

**Date**: 2026-10-10

## 1. Proveedor comercial

- **Decision**: Este incremento no integra un proveedor comercial y no añade un SDK de modelo de lenguaje. El adaptador predeterminado devuelve una propuesta vacía dentro del proceso. Un segundo adaptador lee un texto estructurado ya presente en la configuración y tampoco abre una conexión.
- **Rationale**: La spec cierra el incremento sin cuenta ni clave. La constitución prohíbe anticipar una dependencia que el hito no necesita para cumplirse. Un cliente real haría las pruebas dependientes de red y de un secreto.
- **Alternatives considered**: Cliente de un proveedor concreto con clave en el entorno. Se rechaza: no hay proveedor elegido y el fallo de red no puede ser requisito para evaluar. Dejar el puerto sin ninguna implementación. Se rechaza: no demostraría el intercambio.

## 2. Dónde vive la propuesta

- **Decision**: `EvaluacionOferta.evaluar` sigue calculando estado, coincidencias, brechas y datos pendientes, y deja la lista de observaciones vacía. El caso de uso adjunta después solo las observaciones ya aceptadas, con `conObservaciones`. Esa copia no modifica los cuatro resultados calculados.
- **Rationale**: La constitución exige que el modelo proponga por un puerto y que una respuesta inválida no sustituya el estado calculado. Meter la propuesta dentro de `evaluar` mezclaría el contraste con el proveedor.
- **Alternatives considered**: Un tipo paralelo solo en la aplicación, invisible para quien consulta la evaluación. Se rechaza: la spec pide ver la propuesta en la evaluación que ya se consulta. Un endpoint nuevo. Se rechaza: no existe canal HTTP de evaluación y este hito no abre uno.

## 3. Qué observación se acepta

- **Decision**: Cada candidata tiene `texto` y `fragmento`, ambos obligatorios y sin quedar en blanco. El fragmento, sin distinguir mayúsculas, tiene que aparecer en el corpus de la oferta y del perfil (título, empresa, descripción, ubicación, modalidad, salario publicado si existe, tecnologías, y los campos equivalentes del perfil). Se rechaza la observación entera, sin reescribirla, cuando el texto contiene un número que no está en el corpus, una modalidad que no está en el corpus (`REMOTE`, `HYBRID`, `ONSITE`, `remoto`, `hibrido`, `híbrido`, `presencial`), un estado de evaluación (`COMPATIBLE`, `COMPATIBLE_CON_BRECHAS`, `NO_COMPATIBLE`) o un token técnico con mayúscula inicial y al menos tres caracteres que no esté en el corpus (`Java` se acepta si la oferta lo dice; `Rust` se rechaza si no aparece). Si ninguna candidata sobrevive, la propuesta queda vacía. Si el texto estructurado no se puede leer, se descarta entero y no se rescatan trozos.
- **Rationale**: La spec separa la cita (el fragmento tiene que existir) de la invención (salario, modalidad, tecnología o estado que no están en los datos). Rechazar la observación completa evita guardar una frase a medias.
- **Alternatives considered**: Aceptar cualquier texto cuyo fragmento exista. Se rechaza: un fragmento citado podría acompañar un salario inventado. Interpretar el texto con otro modelo. Se rechaza: volvería a sustituir la regla determinista.

## 4. Fallo, tiempo e ilegibilidad

- **Decision**: El puerto devuelve disponible (lista, quizá vacía) o no disponible (`FALLO`, `TIEMPO_AGOTADO`, `ILEGIBLE`). El caso de uso aplica un límite de 2 segundos. Si el puerto no responde a tiempo, lanza o devuelve fallo, la evaluación se guarda igual con cero observaciones. El adaptador predeterminado responde al instante con la lista vacía y no consume ese límite.
- **Rationale**: El criterio de M5 es que el fallo del proveedor no corrompa el dominio. El límite tiene que existir en el caso de uso para que un adaptador lento no deje la evaluación a medias, aunque el adaptador de este hito no salga del proceso.
- **Alternatives considered**: Propagar el error y no guardar la evaluación. Se rechaza: la spec exige completarla. Reintentar el proveedor. Se rechaza: no hay proveedor y un reintento no cambia el cálculo.

## 5. Persistencia

- **Decision**: Migración `V5` crea `evaluacion_observacion`, hija de `evaluacion`, de solo inserción. Sin filas significa propuesta vacía. No se actualizan las filas de una evaluación ya guardada. Una segunda evaluación del mismo par inserta otro agregado.
- **Rationale**: M4 ya guarda el agregado y su evidencia como inserción. La propuesta viaja con esa evaluación para que, al reiniciar, se lea el mismo número de observaciones.
- **Alternatives considered**: Columna de texto libre en `evaluacion`. Se rechaza: no distingue varias observaciones ni obliga el fragmento. No persistir la propuesta. Se rechaza: SC-006 pide leerla después de reiniciar.

## 6. Intercambio

- **Decision**: Puerto `PropuestaLenguajePort`. Bean predeterminado `vacia` si no hay otro. Bean alternativo `texto`, activo con `jobagent.lenguaje.adaptador=texto`, que interpreta la propiedad `jobagent.lenguaje.texto`. Ninguno usa red. El dominio no importa Spring ni un SDK.
- **Rationale**: Dos implementaciones del mismo puerto bastan para sustituir quién propone. La comprobación cambia la propiedad o inyecta el otro adaptador y compara el estado calculado.
- **Alternatives considered**: Un solo adaptador con una bandera interna. Se rechaza: no hay una segunda implementación que sustituir. Elegir el proveedor en este hito. Se rechaza: ver decisión 1.
