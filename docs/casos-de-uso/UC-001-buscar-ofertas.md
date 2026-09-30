# UC-001 — Buscar ofertas de empleo

- Estado: Implementado y verificado (M1–M3).
- Última actualización: 2026-09-30.
- Actor: persona que busca oportunidades laborales.
- Hitos: primer incremento de M1–M3 implementado; integración pública real proyectada en M4.1.

## Objetivo

Recibir criterios explícitos y devolver ofertas coincidentes con procedencia verificable. El primer incremento usa datos simulados reproducibles y funciona sin LLM, base de datos ni servicios externos.

## Alcance

Búsqueda y normalización de resultados. Buscar no registra ofertas ni ejecuta evaluaciones automáticamente en este incremento. El flujo completo buscar → registrar → evaluar → consultar se construye después mediante casos de uso separados.

Quedan fuera: postulaciones, login en portales, scraping de sesiones privadas, inferencia salarial y ranking con IA.

## Precondiciones

Existe al menos una fuente simulada habilitada y conectada al puerto de salida.

## Entrada propuesta

Endpoint futuro: `POST /api/ofertas/busquedas`.

```json
{
  "cargo": "Backend",
  "tecnologias": ["Java", "Kafka"],
  "ubicacion": "Colombia",
  "modalidad": "REMOTE",
  "limite": 20
}
```

- Cargo: obligatorio y no vacío tras eliminar espacios externos.
- Tecnologías: opcionales; lista vacía no restringe tecnologías.
- Ubicación: opcional; vacío significa sin filtro.
- Modalidad: opcional; valores iniciales REMOTE, HYBRID y ONSITE.
- Límite: entero entre 1 y 100; predeterminado 20; límite global de la respuesta.

## Salida propuesta

```json
{
  "ofertas": [
    {
      "idFuente": "sim-001",
      "fuenteId": "SIMULADA",
      "titulo": "Senior Java Backend Engineer",
      "empresa": "Empresa de ejemplo",
      "descripcion": "Java, Spring Boot, Kafka y AWS",
      "ubicacion": "Colombia",
      "modalidad": "REMOTE",
      "urlOriginal": "https://example.com/jobs/sim-001",
      "fechaPublicacion": null,
      "salario": null
    }
  ],
  "fuentesConsultadas": ["SIMULADA"],
  "advertencias": []
}
```

Datos y URL del ejemplo son ficticios. La implementación debe conservar la fecha de captura y procedencia. Los campos no publicados quedan desconocidos; no se inventan valores. Salario tendrá moneda y período cuando se modele.

## Flujo principal

1. Validar y normalizar criterios.
2. Seleccionar fuentes habilitadas que tengan adaptador disponible.
3. Consultar el puerto BuscarOfertasPort con los criterios.
4. Normalizar los resultados y aplicar las reglas de coincidencia.
5. Eliminar duplicados de la misma fuente por identificador estable.
6. Ordenar por fuenteId e idFuente para obtener resultados reproducibles y aplicar el límite global.
7. Devolver ofertas, fuentes consultadas y advertencias.

## Reglas iniciales de coincidencia

- Cargo: coincidencia parcial en título, sin distinguir mayúsculas.
- Tecnologías: todas las solicitadas deben figurar en el conjunto normalizado de tecnologías de la oferta simulada.
- Ubicación: coincidencia parcial, sin distinguir mayúsculas. Remoto no implica que cualquier país sea elegible.
- Modalidad: igualdad exacta; modalidad desconocida no satisface un filtro explícito.
- Todos los filtros informados se combinan con AND.
- No se fusionan automáticamente ofertas distintas de diferentes portales.

Estas reglas corresponden al primer adaptador simulado; el mapeo de filtros de cada portal real se documentará antes de integrarlo.

## Alternativas y errores

- Criterios inválidos: error de validación; el adaptador HTTP lo traduce a 400.
- Búsqueda correcta sin coincidencias: 200 con lista vacía.
- Fuente deshabilitada: no se consulta.
- Ninguna fuente disponible: error explícito de disponibilidad; HTTP 503 propuesto.
- Algunas fuentes fallan: devolver resultados válidos con advertencias estructuradas por fuente.
- Todas las fuentes fallan: error de disponibilidad, no una búsqueda vacía exitosa.

## Puertos y adaptadores previstos

| Responsabilidad | Elemento | Ubicación |
|---|---|---|
| Criterios y oferta normalizada | CriteriosBusqueda, OfertaLaboral | opportunities/domain |
| Contrato de entrada | BuscarOfertasUseCase | opportunities/application/port/in |
| Orquestación | BuscarOfertasService | opportunities/application/service |
| Contrato de fuente | BuscarOfertasPort | opportunities/application/port/out |
| Datos reproducibles | FuenteOfertasSimuladaAdapter | opportunities/adapter/out |
| HTTP y validación | BuscarOfertasController | opportunities/adapter/in/web |
| Composición | Configuración Spring | configuration |

Se conserva el nombre BuscarOfertasPort del plan existente. Dominio y aplicación no dependen de HTTP, Spring, SDKs de portales ni proveedores de IA. No se define un aggregate para búsqueda hasta encontrar invariantes que lo justifiquen.

## Criterios de aceptación

- Buscar Backend + Java + Kafka + Colombia + REMOTE devuelve sim-001.
- Una oferta Java sin Kafka queda fuera de esa búsqueda.
- Una oferta ONSITE queda fuera cuando se solicita REMOTE.
- Cambiar mayúsculas en cargo y tecnologías conserva el resultado.
- Sin coincidencias se obtiene una lista vacía sin error.
- Cargo vacío o límite fuera de rango produce validación fallida.
- Un identificador duplicado de la misma fuente aparece una sola vez.
- Una fuente deshabilitada nunca es invocada.
- Una fuente fallida y otra correcta producen resultados y advertencia; el fallo total es explícito.
- Los resultados no exceden el límite solicitado.
- Salario y fecha ausentes permanecen desconocidos.

La verificación prevista usa pruebas del caso de uso con fuentes falsas y pruebas HTTP al incorporar el endpoint. Este documento no afirma que esas pruebas ni el endpoint existan.

## Decisiones y pendientes

Confirmado: comenzar con fuente simulada y búsqueda determinista; IA posterior para evaluación.

Pendiente: implementar el caso, concretar catálogo configurable y verificar mecanismos permitidos de integración para LinkedIn, Indeed, Glassdoor y otras fuentes. No se promete acceso funcional a estos portales.

## Historial

- 2026-09-30: definición inicial del primer caso de uso implementable.
