# Research: Persistencia de oportunidades

## 1. Motor de persistencia

**Decision**: PostgreSQL, con esquema versionado por Flyway y acceso JPA solo en adaptadores de salida. Spring Boot sigue en 4.0.8 y Java en 27.

**Rationale**: La constitución fija esa pila para M4. El dominio y los casos de uso ya existen sin Spring; el adaptador en memoria de evaluaciones demuestra el puerto que hay que sustituir.

**Alternatives considered**: Seguir con el mapa en memoria (no sobrevive a un reinicio). Archivos JSON locales (no dan unicidad concurrente ni migraciones). MongoDB u otro motor (fuera de la constitución).

## 2. Dependencias de Spring Boot 4

**Decision**: Añadir, sin versión explícita para que las resuelva el parent 4.0.8:

- `spring-boot-starter-data-jpa`
- `spring-boot-starter-flyway` (no basta `flyway-core`: en Boot 4 la autoconfiguración vive en el starter)
- `org.flywaydb:flyway-database-postgresql` si el starter no lo trae de forma transitiva
- `org.postgresql:postgresql` en runtime
- En test: `spring-boot-testcontainers`, `org.testcontainers:testcontainers-junit-jupiter` y `org.testcontainers:testcontainers-postgresql`

**Rationale**: El `pom.xml` actual ya usa el nombre modular `spring-boot-starter-webmvc`. Flyway 10 separó el soporte por base de datos. Testcontainers 2, alineado con ejemplos de Boot 4, renombró los artefactos `junit-jupiter` y `postgresql`.

**Alternatives considered**: Liquibase. Se descarta porque el hito ya nombró migraciones versionadas y Flyway es el complemento directo del starter de Boot 4. Si la resolución Maven falla con los nombres de Testcontainers 2, la implementación prueba los artefactos históricos `org.testcontainers:junit-jupiter` y `org.testcontainers:postgresql` antes de cambiar de enfoque.

## 3. Dónde vive cada regla

**Decision**: JPA, Flyway y PostgreSQL no entran al paquete `opportunities.domain` ni a los servicios de aplicación. Las entidades JPA y los repositorios Spring Data viven en `opportunities.adapter.out.persistence`. Una regla nueva de identidad, normalizar la dirección pública, vive en el dominio y se prueba sin Spring.

**Rationale**: El principio I prohíbe JPA en el dominio. La identidad de una oferta es una regla de negocio de esta spec, no un detalle de tabla.

**Alternatives considered**: Normalizar la URL solo en el adaptador SQL. Se descarta porque la deduplicación dejaría de poder fallar en una prueba de dominio.

## 4. Identidad de la oferta guardada

**Decision**: La identidad persistida es `fuenteId` más la dirección pública normalizada. Es distinta de la clave con la que UC-001 deduplica el resultado de una búsqueda (`fuenteId` + `idFuente`).

Normalización, en una función pura:

- recortar espacios
- rechazar vacío, ausencia de esquema o un esquema distinto de `http` y `https`
- esquema y host en minúsculas
- quitar el fragmento
- quitar el puerto 80 en `http` y el 443 en `https`
- quitar la barra final del path, salvo que el path sea solo `/`
- conservar path y query con su capitalización original

El alta rechaza una oferta sin `urlOriginal`. Esas ofertas pueden seguir apareciendo en una búsqueda; no se pueden registrar.

**Rationale**: La spec fija la identidad en fuente más dirección pública. UC-001 ya deduplica por `idFuente` dentro de una respuesta y esta spec no cambia esa búsqueda. Mezclar las dos claves fusionaría ofertas distintas o duplicaría la misma publicación cuando el portal repite el identificador con otra URL.

**Alternatives considered**: Reutilizar `fuenteId:idFuente` como clave primaria. Se descarta porque contradice FR-004 y FR-006. Fusionar por empresa y título. Se descarta por SC-006.

## 5. Qué actualiza una reimportación

**Decision**: La segunda captura de la misma identidad no crea otra oferta. En una sola transacción se agrega una procedencia con el momento de captura y una copia de lo que trajo esa captura, nulos incluidos. Los campos vigentes de la oferta pasan a ser los de la última captura: un salario, una fecha o una modalidad ausentes en esa captura quedan ausentes en la vista vigente. El valor publicado en una captura anterior sigue en su procedencia, no se copia hacia adelante.

**Rationale**: FR-005 pide conservar cada captura. FR-007 prohíbe completar lo que no vino. Guardar el nulo en la procedencia y también en la vista vigente evita inventar continuidad.

**Alternatives considered**: Conservar el último salario no nulo. Se descarta porque convertiría un dato ausente de la captura nueva en un dato presente. Crear otra oferta si cambia el título. La spec lo prohíbe.

## 6. Concurrencia

**Decision**: Restricción única en (`fuente_id`, `url_normalizada`). El alta inserta oferta y procedencia en una transacción. Si dos altas simultáneas chocan, la perdedora relee la fila única y agrega su procedencia. El resultado observable es una sola oferta.

**Rationale**: El caso borde de la spec pide exactamente ese resultado. La restricción de base es la garantía; el reintento es la traducción a un solo agregado.

**Alternatives considered**: Bloqueo en memoria. No sobrevive a dos procesos ni a un reinicio.

## 7. Evaluaciones inmutables y su evidencia

**Decision**: No se modifica `EvaluacionOferta.evaluar`. Cada llamada sigue creando un id nuevo y el repositorio solo inserta. `idOferta` sigue siendo `oferta.idFuente()`, como hoy.

Además, el mismo guardado persiste una copia inmutable del perfil y de la oferta usados, incluida la identidad normalizada. Reimportar la oferta actualiza la oferta vigente y no reescribe evaluaciones ya guardadas. La evaluación sigue apuntando a la misma identidad (`fuenteId` + URL normalizada) guardada en esa copia.

**Rationale**: FR-012 pide no borrar evaluaciones previas. FR-002 y FR-015 piden recuperar el mismo resultado y la evidencia. Cambiar el cálculo de compatibilidad violaría el principio II y la suposición de que las reglas no cambian.

**Alternatives considered**: Guardar solo el texto de coincidencias, brechas y datos pendientes. Alcanza para releer el estado, pero no el perfil ni la oferta que lo produjeron. Una columna JSON. Se evita para no acoplar el adaptador al cambio de Jackson 2 a Jackson 3 de Boot 4; las copias van en tablas hijas.

## 8. La búsqueda recuerda, no registra

**Decision**: `BuscarOfertasService` escribe un registro de búsqueda y no llama al repositorio de ofertas ni al de evaluaciones. El constructor actual, el que solo recibe fuentes, delega en un registro nulo para no romper las pruebas de UC-001. El bean de Spring inyecta el registro real.

El registro se confirma en su propia escritura, antes de propagar `FuentesNoDisponiblesException`. Así sobrevive aunque el caso de uso siga fallando como hoy cuando no hay fuentes habilitadas o cuando todas fallan.

Resultados distinguibles en el registro:

- `CON_RESULTADOS`
- `RESULTADO_VACIO`: al menos una fuente consultada, cero ofertas y cero advertencias
- `FALLO_PARCIAL`: alguna fuente respondió y alguna falló
- `BUSQUEDA_FALLIDA`: ninguna fuente respondió y hay advertencias; después se lanza la excepción actual
- `SIN_FUENTES_HABILITADAS`: ninguna fuente habilitada; después se lanza la excepción actual

**Rationale**: FR-009, FR-010 y FR-011, más el caso borde de fuentes deshabilitadas. Mantener la excepción conserva el contrato HTTP de UC-001.

**Alternatives considered**: Hacer que la búsqueda también registre ofertas. Lo prohíben la constitución y FR-009. Cambiar el HTTP para que "sin fuentes" responda 200. Cambiaría UC-001, que esta spec trata como línea base.

## 9. Fuentes configuradas

**Decision**: Tabla sembrada por la primera migración. `SIMULADA` queda `DISPONIBLE` y habilitada. LinkedIn, Indeed y Glassdoor quedan `PENDIENTE` y deshabilitadas, sin adaptador de búsqueda. Al arrancar, la configuración lee ese estado y construye `FuenteOfertasSimuladaAdapter` con el booleano guardado. No hay API de administración en este incremento.

**Rationale**: FR-013 pide que el estado sobreviva al reinicio. La constitución prohíbe tratar un portal configurado como integración funcional. El adaptador simulado ya recibe `habilitada` por constructor; las pruebas unitarias de ese adaptador no necesitan base de datos.

**Alternatives considered**: Dejar el booleano solo en código. No sobrevive a un cambio ni queda registrado como catálogo. Consultar la tabla en cada `estaHabilitada()` desde el dominio. Metería infraestructura en el puerto de búsqueda, que hoy es un catálogo en memoria reproducible.

## 10. Sin canal HTTP nuevo

**Decision**: No se agregan controladores. Registrar, recuperar una evaluación, leer una oferta guardada y leer un registro de búsqueda son casos de uso de aplicación. `POST /api/ofertas/busquedas` conserva su cuerpo de respuesta. Las pruebas de integración con Testcontainers son la lectura que demuestra el reinicio.

**Rationale**: La spec asume que este incremento no abre un canal nuevo. UC-002 todavía no tiene HTTP; inventarlo aquí adelantaría un caso de uso de consulta que el propio índice de `docs/` deja pendiente.

**Alternatives considered**: `GET /api/ofertas/evaluaciones/{id}` y `POST /api/ofertas`. Servirían a un humano sin mirar pruebas, pero contradicen esa suposición. Pueden ser otra spec.

## 11. Pruebas y arranque local

**Decision**: `./mvnw test` necesita Docker. Las pruebas nuevas de adaptador y el `@SpringBootTest` existente arrancan PostgreSQL con `@ServiceConnection`. El perfil local documenta `SPRING_DATASOURCE_URL`, usuario y contraseña, más un `docker-compose.yml` con `postgres:17` para correr la aplicación fuera de las pruebas. No se versionan credenciales reales.

**Rationale**: Añadir JPA hace que el contexto de Spring pida un datasource. Hoy `JobAgentApplicationTests` solo carga el contexto; sin contenedor, ese test se rompe. El principio IV pide prueba de integración en la frontera de persistencia.

**Alternatives considered**: H2 en memoria. No demuestra el SQL ni la restricción única de PostgreSQL. Excluir la autoconfiguración JPA del test de contexto y no levantar nunca la aplicación completa. Dejaría SC-001 sin una prueba de reinicio real.

## 12. Escala

**Decision**: Un solo usuario local. El objetivo de este incremento es correctitud, no caudal: recuperar el 100 % de las evaluaciones, dejar 1 oferta tras 10 importaciones y no escribir ofertas ni evaluaciones al buscar. No se fija un objetivo de peticiones por segundo.

**Rationale**: La spec no pide rendimiento de producción. Inventar uno no cambia el diseño.

**Alternatives considered**: Caché, réplicas o particionado. Fuera de M4.
