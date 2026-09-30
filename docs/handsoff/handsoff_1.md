Proyecto: agente de búsqueda y evaluación de empleos
Repositorio: https://github.com/gustavosotocano/java_agent_job
Objetivo
Construir un agente con Spring Boot 4 para aprender y aplicar SOLID, Clean Architecture, arquitectura hexagonal y DDD. Buscará ofertas públicas y posteriormente las evaluará contra un perfil profesional usando un LLM. No enviará postulaciones automáticamente.
Estado actual
- El proyecto local ya está integrado con GitHub.
- Se compartió job-agent.zip para revisar la estructura.
- Existe una base M0 con Maven Wrapper, configuración, health check y paquetes de dominio, aplicación, puertos, adapters y configuración.
- Se actualizaron PLAN.md y README.md y se creó documentación:
    - docs/README.md
    - docs/casos-de-uso/UC-001-buscar-ofertas.md
    - docs/casos-de-uso/PLANTILLA.md
- Los cambios se entregaron en job-agent-docs-update.zip. Falta confirmar que fueron extraídos y subidos a GitHub.
- UC-001 está documentado, todavía no implementado.
- No fue posible acceder directamente al repositorio desde este entorno: GitHub solicitó autenticación.
  Primer caso de uso: UC-001 — Buscar ofertas de empleo
  Entrada: cargo, tecnologías, ubicación, modalidad y límite de resultados.
  Salida: ofertas normalizadas con título, empresa, descripción, ubicación, modalidad, salario opcional, URL original, fuente y fechas disponibles; además, fuentes consultadas y advertencias.
  Primer incremento:
- Fuente simulada con datos reproducibles.
- Sin LLM, persistencia ni servicios externos.
- Endpoint propuesto: POST /api/ofertas/busquedas.
- Buscar no registra ni evalúa ofertas automáticamente.
  Elementos previstos:
  Responsabilidad	Elemento
  Dominio	CriteriosBusqueda, OfertaLaboral
  Puerto de entrada	BuscarOfertasUseCase
  Servicio de aplicación	BuscarOfertasService
  Puerto de salida	BuscarOfertasPort
  Adapter simulado	FuenteOfertasSimuladaAdapter
  Adapter HTTP	BuscarOfertasController


Reglas documentadas: filtros combinados con AND, tecnologías solicitadas presentes, resultados limitados y reproducibles, deduplicación por fuente e identificador, datos ausentes sin inventar y distinción entre búsqueda vacía y búsqueda fallida.
Decisión sobre portales
Usar un catálogo configurable y un adapter por integración, todos detrás de BuscarOfertasPort.
- YAML: configuración operativa — identificador, habilitación, estado y capacidades.
- Markdown en docs/portales/: acceso, filtros, paginación, normalización, límites y errores de cada fuente.
- Skill futura: instrucciones para que el agente elija y utilice las herramientas disponibles.
- El Markdown no ejecuta la integración ni activa un portal por sí mismo.
  LinkedIn, Indeed y Glassdoor son fuentes candidatas. Sus mecanismos de acceso deben verificarse antes de implementar adapters. No se utilizarán cuentas personales ni automatización de login.
  Se compartió un ejemplo de skill linkedin-search que llama a un CLI TypeScript mediante Bun. Solo se proporcionó el texto de la skill, no el código del CLI; sus afirmaciones técnicas no están verificadas.
  Plan restante
  Dominio y aplicación → REST → PostgreSQL y deduplicación → primera fuente real → LLM con salida validada → agente con herramientas y ejecución acotada → observabilidad y revisión de arquitectura.
  Pendiente técnico detectado
  El pom.xml recibido declara Java 27, mientras VERIFICATION.md registra una validación previa con Java 17.0.20. Confirmar la versión objetivo y corregir esa inconsistencia antes de compilar. El POM declara Spring Boot 4.0.8.
  Próximo paso
  Confirmar la documentación incorporada y la versión de Java. Después implementar UC-001 como incremento completo: dominio, puertos, servicio, adapter simulado, configuración Spring, endpoint y pruebas de filtros, validación y errores. Actualizar el caso de uso y el plan para reflejar lo realmente implementado.