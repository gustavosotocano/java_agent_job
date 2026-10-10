# Quickstart: Adaptador de lenguaje intercambiable

## Prerrequisitos

Java 27, Maven Wrapper y Docker. Las pruebas de reinicio usan el PostgreSQL de Testcontainers que ya usa M4. No hace falta una clave de proveedor.

## Comprobar

Desde `job-agent`:

```bash
./mvnw test
```

El resultado esperado es la suite en verde, incluidas las pruebas nuevas de este incremento.

## Qué tiene que cumplirse

1. `EvaluacionOferta.evaluar` deja la propuesta vacía y el estado igual que antes de este hito. Adjuntar observaciones no cambia estado, coincidencias, brechas ni datos pendientes.
2. El mismo perfil y la misma oferta, con propuesta vacía, con una observación aceptada y con un puerto que falla, producen el mismo estado y el mismo desglose calculado. La evaluación se guarda en los tres casos.
3. De dos candidatas, se conserva la que cita un fragmento presente y se descarta la que inventa un número, una modalidad, un estado o un token técnico ausente del corpus. Si el texto estructurado no se lee, quedan cero observaciones.
4. Con `jobagent.lenguaje.adaptador` en su valor predeterminado no hay llamada de red. Con `texto`, el mismo par conserva el estado y solo cambia el número de observaciones.
5. Una evaluación guardada con observaciones, o con cero, se lee igual al abrir otro contexto contra la misma base. La segunda evaluación no reescribe la primera.
6. Si el puerto tarda más que el límite, la evaluación igual se guarda y la propuesta queda vacía.
7. Buscar no registra ni evalúa. No aparece un controlador nuevo. `pom.xml` no gana un SDK de lenguaje.

El orden de la constitución se mantiene: primero la regla de dominio sin Spring, después el caso de uso con un doble, después el adaptador y el reinicio contra PostgreSQL.
