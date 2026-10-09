# UC-001 — Buscar ofertas de empleo

- Estado: Implementado. Cada búsqueda queda registrada. Buscar no registra ofertas ni evalúa.
- Última actualización: 2026-10-09.
- Actor: persona que busca oportunidades laborales.
- Hitos: M1–M3 entregados. M4 persiste el registro de la búsqueda y el estado de las fuentes. Los portales reales siguen en M4.1 y el LLM en M5.

## Objetivo

Recibir criterios explícitos y devolver ofertas coincidentes con procedencia verificable. El incremento actual usa la fuente simulada, sin LLM ni portales reales. Cada búsqueda se guarda con sus criterios, fuentes, advertencias y resultado.

## Alcance

Búsqueda, normalización de resultados y registro del resultado de esa búsqueda. Buscar no inserta ofertas ni evaluaciones, y no invoca esos casos de uso. Registrar una oferta y evaluarla siguen siendo pasos separados.

Quedan fuera: postulaciones, login en portales, scraping de sesiones privadas, inferencia salarial y ranking con IA.

## Precondiciones

Existe al menos una fuente simulada habilitada y conectada al puerto de salida.

## Entrada propuesta

Endpoint: `POST /api/ofertas/busquedas`. El cuerpo de la respuesta no cambia con la persistencia.

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
8. Confirmar el registro de la búsqueda antes de devolver o de propagar el error de disponibilidad. El resultado es `CON_RESULTADOS`, `RESULTADO_VACIO`, `FALLO_PARCIAL`, `BUSQUEDA_FALLIDA` o `SIN_FUENTES_HABILITADAS`.

## Reglas iniciales de coincidencia

- Cargo: coincidencia parcial en título, sin distinguir mayúsculas.
- Tecnologías: todas las solicitadas deben figurar en el conjunto normalizado de tecnologías de la oferta simulada.
- Ubicación: coincidencia parcial, sin distinguir mayúsculas. Remoto no implica que cualquier país sea elegible.
- Modalidad: igualdad exacta; modalidad desconocida no satisface un filtro explícito.
- Todos los filtros informados se combinan con AND.
- No se fusionan automáticamente ofertas distintas de diferentes portales.

Estas reglas corresponden al primer adaptador simulado; el mapeo de filtros de cada portal real se documentará antes de integrarlo.

## Alternativas y errores

- Criterios inválidos: error de validación; el adaptador HTTP lo traduce a 400. No queda registro.
- Búsqueda correcta sin coincidencias: 200 con lista vacía y registro `RESULTADO_VACIO`.
- Fuente deshabilitada: no se consulta.
- Ninguna fuente habilitada: registro `SIN_FUENTES_HABILITADAS` y después error de disponibilidad (HTTP 503).
- Algunas fuentes fallan y alguna responde: resultados válidos, advertencias y registro `FALLO_PARCIAL`.
- Todas las fuentes fallan: registro `BUSQUEDA_FALLIDA` y después error de disponibilidad. No es una búsqueda vacía exitosa.
- Hay ofertas y ninguna advertencia: registro `CON_RESULTADOS`.

## Puertos y adaptadores previstos

| Responsabilidad | Elemento | Ubicación |
|---|---|---|
| Criterios y oferta normalizada | CriteriosBusqueda, OfertaLaboral | opportunities/domain |
| Contrato de entrada | BuscarOfertasUseCase | opportunities/application/port/in |
| Orquestación | BuscarOfertasService | opportunities/application/service |
| Contrato de fuente | BuscarOfertasPort | opportunities/application/port/out |
| Datos reproducibles | FuenteOfertasSimuladaAdapter | opportunities/adapter/out |
| HTTP y validación | BuscarOfertasController | opportunities/adapter/in/web |
| Registro de la búsqueda | RegistroBusquedaRepository | opportunities/application/port/out |
| Estado de las fuentes | FuenteConfiguradaRepository | opportunities/application/port/out |
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
- Una búsqueda con ofertas no aumenta las ofertas registradas ni las evaluaciones.
- Un resultado vacío, un fallo total y una búsqueda sin fuentes habilitadas quedan distinguibles al leer el registro.

La verificación usa pruebas del caso de uso con fuentes falsas, la prueba HTTP del endpoint y la prueba de persistencia del registro.

## Decisiones y pendientes

Confirmado: comenzar con fuente simulada y búsqueda determinista; IA posterior para evaluación.

Pendiente: verificar mecanismos permitidos de integración para LinkedIn, Indeed, Glassdoor y otras fuentes. Esas fuentes quedan sembradas como `PENDIENTE` y deshabilitadas. No se promete acceso funcional a estos portales.

## Historial

- 2026-09-30: definición inicial del primer caso de uso implementable.
- 2026-10-09: la búsqueda se registra en PostgreSQL y sigue sin registrar ofertas ni evaluar.
