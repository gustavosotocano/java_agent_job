# Quickstart: Verificar acceso a portales públicos

Guía de comprobación. Los escenarios corresponden a [spec.md](spec.md), el modelo a [data-model.md](data-model.md) y los contratos a [contracts/](contracts/).

## Prerrequisitos

- Java 27 y el Maven Wrapper del repositorio
- Docker en ejecución. Las pruebas de persistencia reutilizan PostgreSQL 17 con Testcontainers, como en M4

No hace falta una clave de LinkedIn, Indeed ni Glassdoor. Este incremento no llama a esos portales.

## Comprobación automática

Desde la raíz del repositorio:

```bash
./mvnw test
```

Resultado esperado: la suite de M4 sigue en verde y se suman las pruebas de abajo.

## Escenarios

### 1. Tres dictámenes, ninguna vía comprobada (P1, SC-001)

Leer por `ConsultarDictamenAcceso`.

Esperado: tres dictámenes, uno por `LINKEDIN`, `INDEED` y `GLASSDOOR`. En cada uno `viaComprobada` es falso, el motivo no está vacío, y la clase de acceso, la cobertura, los límites y la actualidad son nulos. `SIMULADA` no aparece. Ver [dictamen-acceso.md](contracts/dictamen-acceso.md).

### 2. El dictamen sobrevive al reinicio (SC-006)

Cerrar el contexto de Spring. Abrir otro contra el mismo contenedor. Volver a listar.

Esperado: los mismos tres `fuenteId`, el mismo valor de `viaComprobada` y el mismo motivo.

### 3. La búsqueda no consulta los portales (P2, SC-002)

Contar filas de `oferta` y `evaluacion`. Ejecutar `POST /api/ofertas/busquedas` con criterios que la fuente simulada sí resuelve.

Esperado: ambos conteos iguales. `fuentesConsultadas` contiene `SIMULADA` y no contiene `LINKEDIN`, `INDEED` ni `GLASSDOOR`. El cuerpo no gana campos. Ver [busqueda-sin-portales-reales.md](contracts/busqueda-sin-portales-reales.md).

### 4. Los portales siguen pendientes (FR-006, FR-009)

Leer `fuente_configurada` después del arranque.

Esperado: `SIMULADA` sigue `DISPONIBLE` y habilitada. Los tres portales siguen `PENDIENTE` y deshabilitados. No existe un adaptador de búsqueda para ellos.

### 5. Un dictamen inválido no se construye (regla de dominio)

En una prueba sin Spring, intentar un dictamen no comprobado sin motivo, uno no comprobado con clase de acceso informada, y uno comprobado cuyo motivo diga que hace falta iniciar sesión.

Esperado: los tres casos se rechazan. No hace falta base de datos.
