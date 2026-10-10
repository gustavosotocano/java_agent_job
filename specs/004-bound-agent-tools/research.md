# Research: Ejecución acotada del agente

**Date**: 2026-10-10

## 1. Quién elige los pasos

- **Decision**: La ejecución recibe la lista de pasos. No hay un modelo que decida el siguiente. No se añade un SDK ni se llama al puerto de propuesta.
- **Rationale**: El hito pide herramientas autorizadas, un máximo y un cierre visible. Elegir el paso con un proveedor reabriría la dependencia que M5 dejó fuera.
- **Alternatives considered**: Un bucle que pregunta al adaptador de lenguaje qué herramienta llamar. Se rechaza: el adaptador predeterminado no tiene propuesta y un proveedor comercial no está en este hito.

## 2. Máximo y cierre

- **Decision**: El máximo es 4. El plan se resuelve antes de llamar a ninguna herramienta. Si aparece un nombre distinto de `buscarOfertas`, `consultarPerfil`, `consultarOferta` o `guardarEvaluacion`, se ejecutan solo los pasos anteriores, el prohibido queda en el resultado como no autorizado y el resto no corre. Si todos los nombres están autorizados y hay más de 4, corren los 4 primeros y el cierre es máximo alcanzado. Si la lista cabe en 4 y todos están autorizados, el cierre es completada. La lista vacía es completada con cero pasos.
- **Rationale**: El número tiene que ser comprobable. Cuatro cubre una búsqueda, las dos consultas y guardar una evaluación, y deja el quinto paso como prueba del tope.
- **Alternatives considered**: Un máximo configurable sin valor. Se rechaza: la spec fija 4. Seguir después de un paso prohibido. Se rechaza: la spec detiene la ejecución.

## 3. Qué hace cada herramienta

- **Decision**: `buscarOfertas` llama al caso de uso de búsqueda que ya existe. `consultarOferta` lee una oferta ya registrada por su identificador. `consultarPerfil` compara el identificador con el perfil de esta ejecución y no guarda perfiles. `guardarEvaluacion` carga la oferta registrada y llama al caso de uso de evaluar, que ya calcula el estado y lo guarda. El paso no trae un estado. Si la oferta no está registrada, no se llama a evaluar.
- **Rationale**: Buscar no registra ni evalúa. No hay ficha de perfiles. El estado tiene que salir de `EvaluacionOferta.evaluar`, no de la lista de pasos.
- **Alternatives considered**: Un almacén nuevo de perfiles. Se rechaza: registrar perfil sigue sin ficha. Aceptar el estado en el paso. Se rechaza: sustituiría el cálculo. Registrar desde el agente la oferta que acaba de buscar. Se rechaza: esa herramienta no está autorizada y buscar seguiría sin registrar.

## 4. Dónde se ve el cierre

- **Decision**: El resultado vuelve con el cierre, los pasos que ocurrieron y los nombres que no se ejecutaron. No hay tabla nueva ni endpoint nuevo. Logs y métricas quedan para M7.
- **Rationale**: La spec pide ver el cierre al terminar, no un historial ni un canal distinto.
- **Alternatives considered**: Persistir cada ejecución. Se rechaza: M7 es la observabilidad del proceso y esta spec no pide releer la ejecución después de reiniciar. Un `POST` nuevo. Se rechaza: no hace falta otro canal para comprobar el caso de uso.
