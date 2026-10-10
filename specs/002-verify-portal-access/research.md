# Research: Verificar acceso a portales públicos

Fecha de la verificación: 2026-10-10. Cada dictamen usa fuentes públicas del propio portal o de su documentación. No se probó login, no se usó una cuenta personal y no se automatizó un navegador.

## 1. LinkedIn: vía no comprobada

**Decision**: LinkedIn queda con vía no comprobada. No se implementa un adaptador de búsqueda.

**Rationale**: La documentación vigente de Talent Solutions (vista `li-lts-2026-04`) describe Job Posting y Apply Connect como APIs para que un socio autorizado publique y gestione avisos, no para que un candidato busque ofertas públicas. LinkedIn indica que no acepta socios nuevos para Job Posting y deriva a Apply Connect, que también exige aprobación, contrato y credenciales. Apply Connect además recoge postulaciones, y la constitución prohíbe postular. Un listado anónimo no documentado como API de búsqueda no es una vía permitida: no tiene contrato, cobertura ni límites publicados para este uso.

**Alternatives considered**: Pedir alta como socio de Talent Solutions. Se descarta en este incremento porque no hay acuerdo ni credenciales, y el producto de esa API no es la búsqueda. Leer HTML público o comprar un agregador. Se descarta porque no es una vía autorizada por LinkedIn y adelantaría una dependencia que el hito no justificó.

## 2. Indeed: vía no comprobada

**Decision**: Indeed queda con vía no comprobada. No se implementa un adaptador de búsqueda.

**Rationale**: Indeed cerró el alta de publishers a finales de 2020, marcó Job Search como obsoleto en 2023 y el endpoint dejó de responder en 2024. La documentación actual de socios (Job Sync, Indeed Apply, Disposition Sync, Sponsored Jobs, Job Update) mueve avisos, candidaturas y datos de empleador hacia Indeed. No devuelve una búsqueda de ofertas para este agente. El Publisher JavaScript Plugin muestra un buscador dentro de un sitio editor, exige alta de socio e identificadores de Indeed, y no entrega avisos normalizados bajo nuestro control. Indeed Apply es postulación, prohibida por la constitución.

**Alternatives considered**: Reutilizar una clave vieja de publisher. Se descarta porque el programa ya no emite claves y el endpoint de búsqueda no responde. Contratar un agregador que revenda avisos de Indeed. Se descarta: no es un proveedor autorizado por Indeed para este proyecto y añadiría una dependencia fuera del hito.

## 3. Glassdoor: vía no comprobada

**Decision**: Glassdoor queda con vía no comprobada. No se implementa un adaptador de búsqueda.

**Rationale**: La API de socios que documentaba búsqueda de empleos y empresas pedía identificador y clave de socio. Ese programa dejó de aceptar altas nuevas hacia 2021 y el endpoint documentado responde como recurso inexistente desde 2025. No hay portal de desarrollador de autoservicio. La vía empresarial actual reparte avisos desde un ATS hacia Glassdoor; no es una búsqueda pública. El contenido de reseñas y salarios que Glassdoor reserva a usuarios con sesión queda fuera: exigiría cuenta o login.

**Alternatives considered**: Llamar a la API histórica con una clave nueva. Se descarta porque no se emiten claves y el endpoint ya no atiende. Leer el sitio público con un navegador automatizado. Se descarta por la misma razón que en LinkedIn: no es un mecanismo permitido ni documentado.

## 4. Qué se implementa

**Decision**: Cero adaptadores nuevos. `LINKEDIN`, `INDEED` y `GLASSDOOR` siguen `PENDIENTE` y deshabilitadas. `SIMULADA` sigue `DISPONIBLE` y habilitada. El dictamen de cada portal se guarda y se puede volver a leer después de reiniciar.

**Rationale**: La spec exige implementar solo la fuente cuya vía esté comprobada, y permite terminar con cero fuentes reales si ninguna lo está. Guardar el dictamen cumple SC-001 y SC-006. No consultar esos portales cumple FR-006 y FR-008.

**Alternatives considered**: Dejar el dictamen solo en `research.md`. Se descarta porque el candidato no podría releerlo desde el agente después de reiniciar. Implementar igual un adaptador contra un agregador de pago. Se descarta por el principio V y por la spec.

## 5. Dónde vive el dictamen

**Decision**: Una fila por portal en PostgreSQL, sembrada por Flyway, leída por un caso de uso. No hay endpoint nuevo. `POST /api/ofertas/busquedas` no cambia su cuerpo ni pasa a consultar estos portales. La clase de acceso, la cobertura, los límites y la actualidad quedan nulos cuando la vía no está comprobada. El motivo es obligatorio en ese caso.

**Rationale**: M4 ya usa PostgreSQL 17, Flyway y JPA solo en adaptadores. El dictamen es un hecho durable, no una sonda en cada arranque. Una sonda en vivo volvería a depender de un acceso que esta investigación declaró no comprobado. El caso de uso de lectura sigue el patrón de consultar una evaluación: sin canal HTTP nuevo.

**Alternatives considered**: Columnas extra en `fuente_configurada`. Se descarta porque el estado de la fuente y el dictamen cambian por motivos distintos: el estado dice si se consulta, el dictamen explica por qué todavía no. Reverificar los portales en cada `./mvnw test`. Se descarta: la prueba sería inestable y repetiría una llamada no autorizada.

## 6. Regla de dominio del dictamen

**Decision**: Un dictamen con vía no comprobada exige motivo y deja nulos la clase de acceso, la cobertura, los límites y la actualidad. Un dictamen con vía comprobada exige esas cuatro descripciones. Si el motivo dice que hace falta cuenta personal o iniciar sesión, la vía no puede figurar como comprobada. Este incremento solo siembra dictámenes no comprobados. No hay transición a vía comprobada dentro de M4.1.

**Rationale**: La spec separa el dictamen negativo del positivo y prohíbe inventar cobertura o límites. La constitución prohíbe tratar lo desconocido como un valor de relleno.

**Alternatives considered**: Guardar la clase de acceso como "ninguna" o el texto "N/A". Se descarta: sería un valor inventado. Un enumerado de motivos cerrado. Se descarta porque el motivo es una explicación legible, no una regla que el dominio deba calcular.
