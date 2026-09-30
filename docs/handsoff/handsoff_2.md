Proyecto: agente de búsqueda y evaluación de empleos
Repositorio: https://github.com/gustavosotocano/java_agent_job
Fecha: 2026-09-30

### Objetivo
Construir un agente con Spring Boot y Java 27 para aprender y aplicar SOLID, Clean Architecture, arquitectura hexagonal y DDD táctico. Busca ofertas públicas y evalúa su compatibilidad contra el perfil profesional del candidato de manera determinista y explicable, proyectando el enriquecimiento con LLMs en hitos posteriores sin dependencias anticipadas.

---

### Estado actual del proyecto
- Entorno de desarrollo validado sobre **Java 27** y **Spring Boot 4.0.8**.
- **UC-001 (Buscar ofertas de empleo):** Totalmente implementado, con adaptador simulado, controlador REST (`POST /api/ofertas/busquedas`), manejo de excepciones y pruebas de integración.
- **UC-002 (Evaluar oferta de empleo):** Totalmente implementado y verificado en su capa de dominio puro, puertos hexagonales, servicio de aplicación y adaptador en memoria.
- **Documentación de arquitectura:** Formalizado el patrón de integración de portales externos mediante CLIs desacoplados en `docs/arquitectura/portal-skills-pattern.md`.
- **Suite de pruebas:** 54 pruebas unitarias y de integración pasando limpiamente sin fallos (`BUILD SUCCESS`).

---

### Caso de uso implementado: UC-002 — Evaluar oferta de empleo

#### 1. Resumen funcional
Permite contrastar una oferta laboral (`OfertaLaboral`) con el perfil profesional de un candidato (`PerfilProfesional`), generando un reporte estructurado y explicable con:
1. **Coincidencias (`Coincidencia`):** Tecnologías comunes dominadas, modalidad de trabajo compatible, ubicación admitida y concordancia salarial.
2. **Brechas (`Brecha`):** Requisitos técnicos faltantes o incompatibilidades de modalidad/ubicación (clasificadas como bloqueantes o no bloqueantes).
3. **Datos Pendientes (`DatoPendiente`):** Información ausente en la oferta o en el perfil (ej. salario no publicado, períodos distintos) que requiere consulta posterior, evitando asumir ceros o penalizaciones falsas.
4. **Estado global (`EstadoEvaluacion`):** `COMPATIBLE`, `COMPATIBLE_CON_BRECHAS` o `NO_COMPATIBLE`.

#### 2. Elementos arquitectónicos construidos

| Responsabilidad | Elemento | Ubicación |
|---|---|---|
| **Modelos de Dominio (DDD)** | `PerfilProfesional`, `EvaluacionOferta` (Aggregate Root), `Coincidencia`, `Brecha`, `DatoPendiente`, `TipoAspecto`, `EstadoEvaluacion` | `opportunities/domain` |
| **Puerto de Entrada** | `EvaluarOfertaUseCase`, `EvaluarOfertaCommand` | `opportunities/application/port/in` |
| **Puerto de Salida** | `EvaluacionOfertaRepository` | `opportunities/application/port/out` |
| **Servicio de Aplicación** | `EvaluarOfertaService` | `opportunities/application/service` |
| **Adaptador en Memoria** | `InMemoryEvaluacionOfertaRepository` | `opportunities/adapter/out/persistence` |
| **Configuración Spring** | `OpportunitiesConfiguration` (registro de beans) | `com.gustavo.jobagent.configuration` |

#### 3. Reglas de negocio clave y determinismo
- **Inmutabilidad y Pureza:** Todos los modelos del dominio son registros (`record`) inmutables y no dependen de Spring ni de librerías externas.
- **Normalización tecnológica:** Comparación insensible a mayúsculas/minúsculas y espacios en blanco (`Java` == `java`).
- **Modalidad y Ubicación:** Incompatibilidad de modalidad presencial para perfiles remotos genera brecha bloqueante (`NO_COMPATIBLE`).
- **Salario transparente:** Contraste condicionado a coincidencia de moneda y período; ante ausencia de datos salariales se genera un `DatoPendiente`.
- **Trazabilidad:** Cada evaluación genera un identificador único y marca temporal (`Instant.now()`), persistiendo en el repositorio.

---

### Pruebas y Verificación
- `PerfilProfesionalTest` (3 tests): Invariantes del perfil, normalización de tecnologías y verificación de dominio técnico.
- `EvaluacionOfertaTest` (7 tests): Compatibilidad total, brechas técnicas, incompatibilidad de modalidad bloqueante, salarios compatibles/incompatibles y detección de datos pendientes.
- `EvaluarOfertaServiceTest` (2 tests): Orquestación del caso de uso y persistencia en el repositorio.
- **Ejecución global (`./mvnw clean test`):** 54 tests ejecutados con éxito (0 fallos, 0 errores, 0 omitidos).

---

### Decisiones técnicas y puntos pendientes
1. **Desacople de LLM (M5):** La lógica de evaluación táctica se mantiene determinista y reproducible. El análisis cualitativo o semántico con modelos de lenguaje se integrará en el Hito M5 como un puerto de enriquecimiento secundario.
2. **Persistencia relacional (M4):** Actualmente el repositorio utiliza una implementación en memoria (`ConcurrentHashMap`) thread-safe. En M4 se añadirá el adaptador PostgreSQL con Spring Data JPA.
3. **Exposición REST de UC-002 (opcional):** Puede agregarse un controlador REST para `POST /api/ofertas/evaluaciones` si se desea consumirlo vía HTTP antes del Hito M4.

---

### Próximos pasos recomendados
1. **Hito M4 — Persistencia y Base de Datos:** Configurar PostgreSQL con Testcontainers / Flyway y crear el adaptador JPA para `OfertaLaboral` y `EvaluacionOferta`.
2. **Hito M4.1 — Primer Skill de Portal Real:** Construir el CLI TypeScript con Bun para búsqueda en portales (ej. LinkedIn) e implementar el adaptador de subproceso en Java.
3. **Hito M5 — Enriquecimiento con LLM:** Integrar cliente de modelo de lenguaje para análisis profundo de descripciones complejas.
