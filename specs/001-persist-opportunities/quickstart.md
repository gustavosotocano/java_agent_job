# Quickstart: Persistencia de oportunidades

Guía de comprobación. Los escenarios corresponden a [spec.md](spec.md), el modelo a [data-model.md](data-model.md) y los contratos a [contracts/](contracts/).

## Prerrequisitos

- Java 27 y el Maven Wrapper del repositorio
- Docker en ejecución (las pruebas de persistencia levantan PostgreSQL 17)

Para correr la aplicación fuera de las pruebas, levantar PostgreSQL y exportar:

```bash
docker compose up -d
export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/jobagent
export SPRING_DATASOURCE_USERNAME=jobagent
export SPRING_DATASOURCE_PASSWORD=jobagent
./mvnw spring-boot:run
```

`docker-compose.yml` forma parte de la implementación de este incremento. No hace falta para `./mvnw test`.

## Comprobación automática

Desde la raíz del repositorio:

```bash
./mvnw test
```

Resultado esperado: la suite anterior sigue en verde y se suman las pruebas de abajo. `JobAgentApplicationTests` carga el contexto contra el contenedor, no contra una base vacía sin servidor.

## Escenarios

### 1. La evaluación sobrevive al reinicio (P1, SC-001)

1. Evaluar una oferta con salario ausente contra un perfil, por `EvaluarOfertaUseCase`.
2. Cerrar el contexto de Spring.
3. Abrir otro contexto contra el mismo contenedor.
4. Leer por `ConsultarEvaluacionUseCase`.

Esperado: el mismo id, el mismo estado, las mismas coincidencias, brechas y datos pendientes, el mismo perfil y la misma oferta de evidencia. El salario de esa evidencia sigue nulo. Ver [evaluacion-almacenada.md](contracts/evaluacion-almacenada.md).

### 2. Dos evaluaciones no se pisan (FR-012)

Repetir la evaluación del mismo par antes de cerrar. Tras el reinicio existen dos ids. El primero no cambió.

### 3. Diez importaciones, una oferta (P2, SC-002, SC-005)

Llamar diez veces a `RegistrarOfertaUseCase` con la misma fuente y la misma dirección pública, variando el título en alguna llamada y omitiendo el salario en todas.

Esperado: una oferta vigente, diez procedencias, salario vigente nulo. Ver [registrar-oferta.md](contracts/registrar-oferta.md).

### 4. Dos portales no se fusionan (SC-006)

Registrar dos ofertas con la misma empresa y el mismo título, distinta `fuenteId` o distinta URL. Esperado: dos ids.

### 5. Choque simultáneo

Dos registros concurrentes de la misma identidad. Esperado: una oferta y dos procedencias.

### 6. Buscar no escribe ofertas ni evaluaciones (P3, SC-003)

Contar filas de `oferta` y `evaluacion`. Ejecutar `POST /api/ofertas/busquedas` con criterios que devuelvan ofertas de la fuente simulada. Volver a contar.

Esperado: ambos conteos iguales. El cuerpo HTTP sigue trayendo ofertas, fuentes consultadas y advertencias. Existe un registro nuevo con resultado `CON_RESULTADOS`.

### 7. Vacío distinto de fallo (SC-004)

- Una búsqueda habilitada cuyos filtros no coinciden con el catálogo simulado: registro `RESULTADO_VACIO`, sin advertencias.
- Una fuente que lanza error y ninguna otra responde: registro `BUSQUEDA_FALLIDA`, con advertencia, y la misma excepción que UC-001 usa hoy.
- Ninguna fuente habilitada: registro `SIN_FUENTES_HABILITADAS` y la excepción actual, sin una advertencia que parezca fallo de un portal consultado.

La lectura del registro basta para separar esos tres casos. Ver [registro-busqueda.md](contracts/registro-busqueda.md).

### 8. La fuente sigue en el mismo estado (FR-013)

Reiniciar y leer `fuente_configurada`. `SIMULADA` sigue disponible y habilitada. `LINKEDIN`, `INDEED` y `GLASSDOOR` siguen pendientes y deshabilitadas. Deshabilitar `SIMULADA` en la tabla, reiniciar y buscar: la llamada termina como búsqueda sin fuentes habilitadas. Ver [fuente-configurada.md](contracts/fuente-configurada.md).
