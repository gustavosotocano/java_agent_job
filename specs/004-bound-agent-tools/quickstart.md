# Quickstart: Ejecución acotada del agente

## Prerrequisitos

Java 27, Maven Wrapper y Docker. No hace falta una clave de proveedor ni un canal nuevo.

## Comprobar

Desde `job-agent`:

```bash
./mvnw test
```

## Qué tiene que cumplirse

1. El dominio, sin Spring, parte una lista: como máximo 4 herramientas autorizadas, corte al primer nombre desconocido y cierre `COMPLETADA`, `LIMITE_ALCANZADO` o `HERRAMIENTA_NO_AUTORIZADA`.
2. Una ejecución con búsqueda y un paso prohibido ejecuta solo la búsqueda, no postula y no cambia el número de ofertas registradas ni el de evaluaciones.
3. Cinco pasos autorizados ejecutan cuatro. Un solo paso autorizado cierra como completada.
4. `consultarPerfil` con otro identificador no crea un perfil. `guardarEvaluacion` con oferta registrada guarda el estado del contraste. Con oferta desconocida no crea evaluación.
5. `pom.xml` no gana dependencias. No aparece un controlador nuevo. Buscar, fuera de esta ejecución, sigue sin registrar ni evaluar.

El orden de la constitución se mantiene: primero la regla de dominio sin Spring, después el caso de uso con dobles.
