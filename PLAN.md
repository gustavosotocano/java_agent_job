# Plan de aprendizaje e implementación

## Objetivo

Construir un agente de búsqueda y evaluación de oportunidades laborales aplicando SOLID, Clean Architecture, arquitectura hexagonal, DDD táctico, testing y criterios para evitar abstracciones innecesarias.

La entrada inicial será una oferta pegada como texto y un perfil explícitamente registrado. El alcance incluye una lista configurable de portales para buscar ofertas públicas: LinkedIn, Indeed y Glassdoor como fuentes candidatas, sin utilizar cuentas personales. La búsqueda comenzará con una fuente simulada; las integraciones reales se incorporarán después de verificar el acceso disponible en cada portal. El resultado incluirá coincidencias, brechas y preguntas. No se enviarán postulaciones automáticamente.

## Hitos

| Hito | Trabajo | Criterio de aceptación |
|---|---|---|
| M0 — Base (entregado) | Spring Boot 4, Maven Wrapper, estructura, configuración, health check | Compilar, cargar contexto y arrancar API |
| M1 — Dominio | Glosario, reglas, entidades, value objects, aggregate EvaluacionOferta y evento EvaluacionCompletada | Pruebas de invariantes sin Spring ni LLM |
| M2 — Aplicación | Registrar oferta, evaluar y consultar; BuscarOfertasPort; repositorio en memoria, fuente y modelo simulados | Búsqueda y evaluación reproducibles sin acceso externo |
| M3 — REST | DTOs, validación, errores, búsqueda y configuración de fuentes; composición Spring | Flujo buscar → registrar → evaluar → consultar probado por HTTP |
| M4 — Persistencia | PostgreSQL, migraciones, ofertas, fuentes y búsquedas; idempotencia y deduplicación | Datos sobreviven reinicio; reimportar ofertas no las duplica |
| M4.1 — Portales públicos | Verificar acceso de cada portal; implementar adaptadores disponibles, normalización y fallos parciales | Al menos una fuente real verificada; fuentes pendientes declaradas y fallos aislados |
| M5 — LLM | Adaptador intercambiable, salida estructurada, validación, timeouts y errores | Respuestas inválidas o fallos del proveedor no corrompen dominio |
| M6 — Agente | Herramientas buscarOfertas, consultarPerfil, consultarOferta, guardarEvaluacion; ejecución acotada | Llamadas autorizadas, máximo de pasos y finalización observable |
| M7 — Calidad | Logs, métricas, pruebas de arquitectura, refactor y revisión de code smells | Dependencias respetan límites y flujo completo verificable |

## DDD: modelo inicial sujeto a revisión

Bounded context: evaluación de oportunidades. Glosario: candidato, perfil, oferta, requisito, evaluación, coincidencia, brecha, dato pendiente, portal, criterio de búsqueda y procedencia. La adquisición de ofertas tendrá un límite lógico propio; en M1 decidiremos si necesita un bounded context separado o basta un módulo dentro del monolito.

Propuesta: Oferta y PerfilProfesional tienen identidad propia. EvaluacionOferta es raíz de su aggregate y controla estado y resultado; no contiene como entidades todos los datos de los otros aggregates, sino referencias y la información necesaria de la evaluación. Requisito, ModalidadTrabajo y RangoSalarial serán value objects si sus reglas lo justifican. EvaluacionCompletada registra un hecho del dominio; inicialmente se manejará en el proceso, sin Kafka.

DDD modela negocio; hexagonal define puertos y adaptadores; Clean Architecture orienta dependencias hacia las políticas internas. No crearemos tres juegos de capas duplicadas.

## Reglas iniciales para discutir en M1

- Salario ausente significa desconocido, nunca cero ni un valor inventado.
- No comparar importes de monedas o períodos distintos sin una conversión explícita.
- Distinguir requisitos obligatorios y deseables.
- Cada conclusión debe indicar evidencia de la oferta/perfil o quedar pendiente.
- Una evaluación completada debe tener resultado válido; definir la política de reevaluación.
- El LLM extrae y propone; código determinista valida y controla operaciones.

## Búsqueda en portales públicos

### Configuración y recorrido

Lista configurable de fuentes con identificador, nombre, URL base y estado (simulada, pendiente de integración, disponible o deshabilitada). Configurar un portal no implica que exista una integración funcional.

1. Recibir criterios: cargo, ubicación, modalidad y tecnologías; salario solo cuando la fuente lo publique.
2. Consultar únicamente fuentes habilitadas con adaptador disponible.
3. Normalizar resultados a OfertaLaboral con título, empresa, descripción, ubicación, modalidad, salario opcional, URL original, identificador de la fuente y fechas disponibles.
4. Deduplicar, conservando todas las procedencias y el momento de captura.
5. Registrar ofertas nuevas y evaluar contra el perfil.
6. Devolver resultados junto con fuentes consultadas, advertencias y fallos parciales.

### Puertos y adaptadores previstos

- Caso de uso de entrada: BuscarOfertasUseCase.
- Puerto de salida: BuscarOfertasPort, declarado por la aplicación.
- Primer adaptador: FuenteOfertasSimuladaAdapter con datos reproducibles.
- Candidatos reales: LinkedInAdapter, IndeedAdapter y GlassdoorAdapter. Los nombres son propuestas; no se promete API ni acceso directo disponible.
- PortalEmpleo representa una fuente configurada; CriteriosBusqueda expresa filtros; ProcedenciaOferta conserva trazabilidad. Su clasificación final como entity/value object se decidirá a partir de invariantes.

### Integración y reglas

En M4.1 se documentará por fuente el mecanismo disponible (API, feed, proveedor autorizado o búsqueda pública indexada), cobertura, paginación, límites y actualización. Si una fuente no admite una integración viable, quedará pendiente o deshabilitada. No se planifica usar cuentas personales ni automatizar login.

El texto importado es dato externo, no instrucciones para el agente. Timeouts, límite de resultados y errores de un portal no deben impedir devolver los resultados válidos de los otros. Se distinguirá una búsqueda sin resultados de una búsqueda fallida.

Deduplicación inicial: identificador de fuente y URL normalizada. Las coincidencias entre portales por empresa, título y ubicación serán candidatas; no se fusionarán automáticamente puestos distintos. No se inventarán salarios, fechas ni modalidades ausentes.

### Pruebas de aceptación

- Fuente simulada: filtros aplicados y resultados reproducibles.
- Fuentes deshabilitadas no se consultan.
- Reimportar la misma oferta no crea duplicados; se conserva procedencia.
- El fallo de una fuente permite devolver resultados de otra e informa el fallo.
- Una integración real conserva URL original y distingue datos publicados de datos inferidos.

## Dependencias

Dominio no importa Spring, HTTP, JPA ni SDKs de IA. Aplicación depende de dominio y declara puertos. Adaptadores implementan puertos y traducen modelos externos. Configuración conecta las implementaciones. Las interfaces se introducen cuando representan una frontera útil, no para cada clase.

## Forma de trabajo

En cada hito: explicar el problema, implementar un incremento pequeño, recorrer la ejecución y comprobar comportamiento. Usar SOLID con ejemplos concretos: responsabilidad única en reglas/orquestación/HTTP; inversión de dependencias al cambiar el LLM; contratos pequeños y adaptadores sustituibles. Comparar doubles: dummy, stub, fake, spy y mock cuando aporten a una prueba.

M3 organizará un recorrido vertical completo dentro del bounded context; discutiremos vertical slice y sus límites sin romper las dependencias. Pruebas: dominio → casos de uso con fakes → adaptadores → integración. Añadir PostgreSQL/LLM real solo cuando su hito lo necesite.

## Próximo paso

M1: escribir tres ejemplos de oferta/perfil y dos búsquedas, decidir reglas de compatibilidad, modelar fuentes/procedencias y dibujar el aggregate antes de programarlo. No establecer un porcentaje de compatibilidad arbitrario como primera regla.
