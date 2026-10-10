# UC-003 — Ejecutar el agente con herramientas acotadas

- Estado: Implementado. La ejecución devuelve el cierre al terminar.
- Última actualización: 2026-10-10.
- Actor: Candidato en búsqueda de empleo.
- Hitos: M6.

## Objetivo y actor

**Actor:** Candidato que pide una lista de pasos.

**Objetivo:** Ejecutar solo buscarOfertas, consultarPerfil, consultarOferta y guardarEvaluacion, con un máximo de 4 pasos y un cierre visible.

## Alcance y exclusiones

### En alcance

- Autorizar esos cuatro nombres, tal cual.
- Detener la ejecución en el primer nombre distinto, sin correr lo que sigue.
- Dejar sin ejecutar lo que pase de 4.
- Buscar sin registrar ofertas ni crear evaluaciones.
- Consultar el perfil de esta ejecución, o desconocido si el identificador no coincide.
- Consultar una oferta ya registrada, o desconocida.
- Guardar la evaluación que calcula el contraste. El paso no elige el estado.

### Exclusiones

- Elegir el siguiente paso con un proveedor de lenguaje.
- Registrar un catálogo de perfiles.
- Registrar la oferta que acaba de devolver una búsqueda.
- Postular o iniciar sesión.
- Historial de ejecuciones, logs y métricas. Eso queda en M7.
- Otro canal para pedir la ejecución.

## Entradas y salidas

### Entrada

Un perfil explícito y una lista ordenada de pasos. Cada paso tiene un nombre y, si aplica, criterios o un identificador. No trae un estado de compatibilidad.

### Salida

El cierre (`COMPLETADA`, `LIMITE_ALCANZADO` o `HERRAMIENTA_NO_AUTORIZADA`), los pasos que ocurrieron y los nombres que no se ejecutaron.

## Reglas de negocio

1. Solo los cuatro nombres se ejecutan.
2. El máximo es 4.
3. Un nombre distinto detiene la lista aunque queden pasos por debajo del máximo.
4. Buscar no registra ni evalúa.
5. guardarEvaluacion usa el contraste determinista. Si la oferta no está registrada, no crea una evaluación.
6. Un perfil u oferta desconocidos siguen desconocidos.

## Criterios de aceptación

- Una búsqueda seguida de `postular` ejecuta la búsqueda, no postula y cierra como herramienta no autorizada.
- Después de buscar, no hay ofertas registradas nuevas ni evaluaciones nuevas.
- Cinco pasos autorizados ejecutan cuatro y muestran el quinto como no ejecutado.
- Un paso autorizado cierra como completada.
- guardarEvaluacion con una oferta registrada guarda el estado del contraste. Con una oferta desconocida no guarda nada.
- consultarPerfil con otro identificador no crea un perfil.

## Historial

- 2026-10-10: primera ejecución acotada. No hay proveedor ni canal nuevo.
